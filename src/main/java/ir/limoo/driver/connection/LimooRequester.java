package ir.limoo.driver.connection;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

import ir.limoo.driver.entity.User;
import ir.limoo.driver.entity.WorkerNode;
import ir.limoo.driver.exception.LimooAuthenticationException;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.JacksonUtils;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LimooRequester implements Closeable {

	private static final transient org.slf4j.Logger logger = LoggerFactory
			.getLogger(LimooRequester.class);

	private static final String LOGIN_URI = "j_spring_security_check";
	private static final String REFRESH_TOKEN_URI = "j_spring_jwt_security_check";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private OkHttpClient httpClient;

	private String limooUrl;
	private User user;

	public LimooRequester(String limooUrl, User user) {
		Logger.getLogger(OkHttpClient.class.getName()).setLevel(Level.FINE);
		this.limooUrl = limooUrl;
		this.user = user;
		CookieManager cookieManager = new CookieManager();
		cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
		httpClient = new OkHttpClient().newBuilder().cookieJar(new JavaNetCookieJar(cookieManager)).build();
	}

	private boolean login() {
		// First try refreshing token
		RequestBody body = new FormBody.Builder().add("j_username", user.getUsername())
				.add("j_password", user.getPassword()).build();
		Request request = new Request.Builder().url(createFullUrl(LOGIN_URI, null)).post(body).build();
		try (Response response = httpClient.newCall(request).execute()) {
			String locationHeader = response.header("Location");
			if (locationHeader != null && locationHeader.toLowerCase().contains("error")) {
				// TODO log
				return false;
			}
		} catch (IOException e) {
			logger.error("", e);
			return false;
		}
		return true;
	}

	public String getAccessToken() throws LimooAuthenticationException, LimooException {
		RequestBody body = RequestBody.create(JSON, "");
		Request request = new Request.Builder().url(createFullUrl(REFRESH_TOKEN_URI, null)).method("POST", body)
				.build();
		try (Response response = executeRequest(request)) {
			return response.header("Token");
		} catch (LimooAuthenticationException e) {
			login();
			try (Response response = executeRequest(request)) {
				return response.header("Token");
			}
		}
	}

	public JsonNode uploadFile(File file, WorkerNode worker) throws LimooException {
		String contentType;
		try {
			contentType = Files.probeContentType(file.toPath());
		} catch (IOException e) {
			throw new LimooException(e);
		}
		MediaType mediaType = MediaType.parse(contentType);
		RequestBody body = new MultipartBody.Builder().setType(MultipartBody.FORM)
				.addFormDataPart(file.getName(), file.getName(), RequestBody.create(mediaType, file)).build();
		Request request = new Request.Builder().url(getFileOperationUrl(worker)).post(body).build();
		try {
			return executeRequestAndGetBody(request);
		} catch (LimooAuthenticationException e) {
			login();
			return executeRequestAndGetBody(request);
		}
	}

	public InputStream downloadFile(String hashCode, String fileName, WorkerNode worker) throws LimooException {
		HttpUrl.Builder urlBuilder = HttpUrl.parse(getFileOperationUrl(worker)).newBuilder();
		urlBuilder.addQueryParameter("hash", hashCode).addQueryParameter("file_name", fileName)
				.addQueryParameter("mode", "download");
		Request request = new Request.Builder().url(urlBuilder.build()).build();
		try {
			Response response = executeRequest(request);
			return response.body().byteStream();
		} catch (LimooAuthenticationException e) {
			login();
			Response response = executeRequest(request);
			return response.body().byteStream();
		}
	}

	public JsonNode executeApiGet(String relativeUrl, WorkerNode worker)
			throws LimooException, LimooAuthenticationException {
		Request request = new Request.Builder().url(createApiUrl(relativeUrl, worker)).build();
		try {
			return executeRequestAndGetBody(request);
		} catch (LimooAuthenticationException e) {
			login();
			return executeRequestAndGetBody(request);
		}
	}

	public JsonNode executeApiPost(String relativeUrl, JsonNode bodyNode, WorkerNode worker)
			throws LimooAuthenticationException, LimooException {
		RequestBody body = RequestBody.create(JSON, bodyNode.toString());
		Request request = new Request.Builder().url(createApiUrl(relativeUrl, worker)).post(body).build();
		try {
			return executeRequestAndGetBody(request);
		} catch (LimooAuthenticationException e) {
			login();
			return executeRequestAndGetBody(request);
		}
	}

	private JsonNode executeRequestAndGetBody(Request request) throws LimooAuthenticationException, LimooException {
		try (Response response = executeRequest(request)) {
			return JacksonUtils.convertStringToJsonNode(response.body().string());
		} catch (IOException e) {
			throw new LimooException(e);
		}
	}

	private Response executeRequest(Request request) throws LimooAuthenticationException, LimooException {
		try {
			Response response = httpClient.newCall(request).execute();
			if (response.code() == 401) {
				response.close();
				throw new LimooAuthenticationException();
			} else if (!response.isSuccessful()) {
				response.close();
				throw new LimooException("Request returned unsuccessfully with status " + response.code()
						+ " and message: " + response.message());
			}
			return response;
		} catch (IOException e) {
			throw new LimooException(e);
		}
	}

	private String getFileOperationUrl(WorkerNode worker) {
		return concatenateUris(worker.getFileUrl(), "v1/files");
	}

	private String createApiUrl(String relativeUrl, WorkerNode worker) {
		String apiPrefix = worker == null ? "api/v1" : "v1";
		return createFullUrl(concatenateUris(apiPrefix, relativeUrl), worker);
	}

	private String createFullUrl(String relativeUrl, WorkerNode worker) {
		return concatenateUris(worker == null ? limooUrl : worker.getApiUrl(), relativeUrl);
	}

	private String concatenateUris(String first, String second) {
		return first + ((first.endsWith("/") || second.startsWith("/")) ? "" : "/") + second;
	}

	@Override
	public void close() {
		// Nothing to do for now.
	}
}
