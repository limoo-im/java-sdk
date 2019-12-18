package ir.limoo.driver;

import java.io.Closeable;

import ir.limoo.driver.connection.LimooRequester;
import ir.limoo.driver.connection.LimooWebsocketEndpoint;
import ir.limoo.driver.entity.Conversation;
import ir.limoo.driver.entity.User;
import ir.limoo.driver.entity.Workspace;
import ir.limoo.driver.event_listener.EventListener;
import ir.limoo.driver.event_listener.EventListenerManager;
import ir.limoo.driver.exception.LimooException;

public class LimooDriver implements Closeable {

	private Workspace workspace;
	private User user;
	private LimooRequester limooRequester;
	private EventListenerManager eventListenerManager;
	private LimooWebsocketEndpoint websocketEndpoint;

	/**
	 * @param limooUrl
	 * @param workspaceKey
	 * @param botUsername
	 * @param botPassword
	 * @throws LimooException
	 */
	public LimooDriver(String limooUrl, String workspaceKey, String botUsername, String botPassword)
			throws LimooException {
		try {
			this.user = new User(botUsername, botPassword);
			this.limooRequester = new LimooRequester(limooUrl, user);
			this.workspace = new Workspace(workspaceKey, limooRequester);
			this.eventListenerManager = new EventListenerManager();
			this.websocketEndpoint = new LimooWebsocketEndpoint(workspace.getWorker(), eventListenerManager,
					limooRequester);
		} catch (LimooException e) {
			this.close();
			throw e;
		} catch (Exception e) {
			this.close();
			throw new LimooException(e);
		}
	}

	public Conversation getConversationById(String conversationId) throws LimooException {
		return workspace.getConversationById(conversationId);
	}

	public void registerEventListener(EventListener eventListener) {
		eventListenerManager.addToListeners(eventListener);
	}

	@Override
	public void close() {
		if (limooRequester != null)
			limooRequester.close();
		if (websocketEndpoint != null)
			websocketEndpoint.close();
	}
}
