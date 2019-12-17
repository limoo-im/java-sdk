package ir.limoo.driver.connection;

import java.io.Closeable;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.atmosphere.wasync.Client;
import org.atmosphere.wasync.ClientFactory;
import org.atmosphere.wasync.Decoder;
import org.atmosphere.wasync.Encoder;
import org.atmosphere.wasync.Event;
import org.atmosphere.wasync.Function;
import org.atmosphere.wasync.Options;
import org.atmosphere.wasync.Request;
import org.atmosphere.wasync.RequestBuilder;
import org.atmosphere.wasync.Socket;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import ir.limoo.driver.entity.WorkerNode;
import ir.limoo.driver.entity.event_listener.EventListenerManager;
import ir.limoo.driver.event.MessageCreatedEvent;
import ir.limoo.driver.exception.LimooAuthenticationException;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.JacksonUtils;

public class LimooWebsocketEndpoint implements Closeable {

	private static final transient Logger logger = Logger.getLogger(LimooWebsocketEndpoint.class);
	
	private static final String MESSAGE_CREATED_EVENT = "message_created";
	private static final String AUTHENTICATION_FAILED_EVENT = "authentication_failed";

	private EventListenerManager eventListenerManager;
	private LimooRequester limooRequester;

	private Socket socket;

	private RequestBuilder requestBuilder;

	public LimooWebsocketEndpoint(WorkerNode worker, EventListenerManager eventListenerManager,
			LimooRequester limooRequester) throws LimooAuthenticationException, LimooException {
		this.eventListenerManager = eventListenerManager;
		this.limooRequester = limooRequester;
		this.createSocket(worker);
		this.connect(true);
	}

	private void createSocket(WorkerNode worker) {
		Client client = ClientFactory.getDefault().newClient();

		requestBuilder = client.newRequestBuilder().method(Request.METHOD.GET).uri(worker.getWebsocketUrl()).transport(Request.TRANSPORT.WEBSOCKET)
				.encoder(new Encoder<JsonNode, String>() {

					@Override
					public String encode(JsonNode jsonNode) {
						return JacksonUtils.serializeObjectAsString(jsonNode);
					}

				})
				.decoder(new Decoder<String, JsonNode>() {
					@Override
					public JsonNode decode(Event e, String s) {
						try {
							return JacksonUtils.convertStringToJsonNode(s);
						} catch (IOException e1) {
							logger.error(e1);
							return null;
						}
					}
				}).transport(Request.TRANSPORT.WEBSOCKET);
		
		Options options = client.newOptionsBuilder().reconnect(false).build();
		socket = client.create(options);
		socket.on(new Function<JsonNode>() {
			@Override
			public void on(JsonNode jsonNode) {
				if (jsonNode.get("event") != null) {
					String event = jsonNode.get("event").asText();
					handleEvent(event, jsonNode);
				}
			}
		}).on(Event.CLOSE, new Function<String>() {
			@Override
			public void on(String t) {
				// TODO test
				close();
				try {
					connect(false);
				} catch (LimooException e) {
					logger.error("", e);
				}
			}
		});
	}

	private int consecutiveConnectionAttempts = 0;
	private static final int MAX_CONSECUTIVE_CONNECTION_ATTEMPTS = 1000000;
	private static final int MAX_INITIAL_CONNECTION_ATTEMPTS = 2;

	private void connect(boolean isInitialConnection) throws LimooAuthenticationException, LimooException {
		try {
			consecutiveConnectionAttempts++;
			attemptToConnect();
			consecutiveConnectionAttempts = 0;
		} catch (LimooAuthenticationException e) {
			throw e;
		} catch (IOException | LimooException e) {
			int allowedConnectionAttempts = isInitialConnection ? MAX_INITIAL_CONNECTION_ATTEMPTS : MAX_CONSECUTIVE_CONNECTION_ATTEMPTS;
			if (consecutiveConnectionAttempts >= allowedConnectionAttempts)
				throw (e instanceof LimooException) ? (LimooException) e : (new LimooException(e));
			try {
				Thread.sleep(getNextConnectionAttemptDelay());
			} catch (InterruptedException e1) {
				logger.error(e1);
			}
		}
	}

	private void attemptToConnect() throws IOException, LimooAuthenticationException, LimooException {
		requestBuilder.header("Cookie", "ACCESSTOKEN=" + limooRequester.getAccessToken());
		socket.open(requestBuilder.build());
	}

	private static final long DELAY_INCREASE_MILLIS = 2000;
	
	private long getNextConnectionAttemptDelay() {
		return consecutiveConnectionAttempts * DELAY_INCREASE_MILLIS;
	}
	
	private void handleEvent(String event, JsonNode eventNode) {
		if (AUTHENTICATION_FAILED_EVENT.equals(event)) {
			// This normally shouldn't happen
			logger.error("Received authentication_failed upon opening websocket");
			socket.close();
		} else if (MESSAGE_CREATED_EVENT.equals(event)) {
			try {
				eventListenerManager.newEvent(new MessageCreatedEvent(eventNode.get("data")));
			} catch (JsonProcessingException e) {
				logger.error("", e);
			}
		}
	}

	@Override
	public void close() {
		socket.close();
	}
}
