package ir.limoo.driver.connection;

import java.io.Closeable;
import java.io.IOException;
import java.net.CookieManager;
import java.net.CookiePolicy;

import com.fasterxml.jackson.databind.JsonNode;

import ir.limoo.driver.entity.User;
import ir.limoo.driver.entity.WorkerNode;
import ir.limoo.driver.exception.LimooAuthenticationException;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.JacksonUtils;
import okhttp3.FormBody;
import okhttp3.JavaNetCookieJar;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LimooRequester implements Closeable {

	private static final String LOGIN_URI = "j_spring_security_check";
	private static final String REFRESH_TOKEN_URI = "j_spring_jwt_security_check";
	private static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");

	private OkHttpClient httpClient;

	private String limooUrl;
	private User user;

	public LimooRequester(String limooUrl, User user) {
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
			// TODO log
			return false;
		}
		return true;
	}

	public String getAccessToken() throws LimooAuthenticationException, LimooException {
		Request request = new Request.Builder().url(createFullUrl(REFRESH_TOKEN_URI, null)).method("POST", null).build();
		try (Response response = executeRequest(request)) {
			return response.header("Token");
		} catch (LimooAuthenticationException e) {
			login();
			try (Response response = executeRequest(request)) {
				return response.header("Token");
			}
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
				throw new LimooAuthenticationException();
			} else if (!response.isSuccessful()) {
				throw new LimooException(); // TODO Add message
			}
			return response;
		} catch (IOException e) {
			throw new LimooException(e);
		}
	}

	private String createApiUrl(String relativeUrl, WorkerNode worker) {
		return createFullUrl(concatenateUris("api/v1", relativeUrl), worker);
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
