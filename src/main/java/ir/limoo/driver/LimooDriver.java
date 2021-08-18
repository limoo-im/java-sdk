package ir.limoo.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.limoo.driver.connection.LimooRequester;
import ir.limoo.driver.connection.LimooWebsocketEndpoint;
import ir.limoo.driver.entity.Bot;
import ir.limoo.driver.entity.Workspace;
import ir.limoo.driver.event.JoinWorkspaceEventListener;
import ir.limoo.driver.event.LimooEventListener;
import ir.limoo.driver.event.LimooEventListenerManager;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.JacksonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LimooDriver implements Closeable {

	private static final transient Logger logger = LoggerFactory.getLogger(LimooDriver.class);

	private static final String GET_SELF_URI_TEMPLATE = "user/items/self";
	private static final String GET_MY_WORKSPACES_URI_TEMPLATE = "user/my_workspaces";
	private static final String GET_WORKSPACE_URI_TEMPLATE = "workspace/items/%s";

	private final LimooEventListenerManager limooEventListenerManager;
	private final LimooRequester requester;
	private final Map<String, Workspace> workspacesMap;
	private final Map<String, LimooWebsocketEndpoint> websocketEndpointsMap;
	private Bot bot;

	public LimooDriver(String limooUrl, String botUsername, String botPassword) throws LimooException {
		limooEventListenerManager = new LimooEventListenerManager();
		requester = new LimooRequester(limooUrl, botUsername, botPassword);
		getAndInitBot();
		workspacesMap = new HashMap<>();
		websocketEndpointsMap = new HashMap<>();
		try {
			getAndInitWorkspaces();
		} catch (Exception e) {
			close();
			throw new LimooException(e);
		}
		handleJoinWorkspaceEvent();
	}

	private void getAndInitBot() throws LimooException {
		JsonNode botNode = requester.executeApiGet(GET_SELF_URI_TEMPLATE, null);
		try {
			bot = JacksonUtils.deserializeObject(botNode, Bot.class);
		} catch (JsonProcessingException e) {
			throw new LimooException(e);
		}
	}

	private void getAndInitWorkspaces() throws LimooException {
		ArrayNode workspacesNode = (ArrayNode) requester.executeApiGet(GET_MY_WORKSPACES_URI_TEMPLATE, null);
		if (workspacesNode == null || workspacesNode.isEmpty()) {
			throw new LimooException("The provided bot isn't member of any workspace.");
		}

		try {
			for (JsonNode workspaceNode : workspacesNode) {
				Workspace workspace = new Workspace(requester);
				JacksonUtils.deserializeIntoObject(workspaceNode, workspace);
				workspacesMap.put(workspace.getKey(), workspace);
				String websocketUrl = workspace.getWorker().getWebsocketUrl();
				if (!websocketEndpointsMap.containsKey(websocketUrl)) {
					websocketEndpointsMap.put(websocketUrl, new LimooWebsocketEndpoint(workspace, limooEventListenerManager));
				}
			}
		} catch (IOException e) {
			throw new LimooException("An error occurred while getting bot's workspaces.");
		}
	}

	private void handleJoinWorkspaceEvent() {
		limooEventListenerManager.addToListeners(new JoinWorkspaceEventListener() {
			@Override
			public void onJoinWorkspace(String userId, String workspaceId) {
				if (userId.equals(bot.getId())) {
					try {
						addWorkspace(workspaceId);
					} catch (LimooException e) {
						logger.error("", e);
					}
				}
			}
		});
	}

	public void addEventListener(LimooEventListener listener) {
		this.limooEventListenerManager.addToListeners(listener);
	}

	public void removeEventListener(LimooEventListener listener) {
		this.limooEventListenerManager.removeFromListeners(listener);
	}

	public Bot getBot() {
		return bot;
	}

	public List<Workspace> getWorkspaces() {
		return new ArrayList<>(workspacesMap.values());
	}

	public Workspace getWorkspaceByKey(String workspaceKey) {
		return workspacesMap.get(workspaceKey);
	}

	private void addWorkspace(String workspaceId) throws LimooException {
		Workspace existingWorkspace = workspacesMap.values().stream()
				.filter(w -> w.getId().equals(workspaceId))
				.findAny().orElse(null);
		if (existingWorkspace == null) {
			JsonNode workspaceNode = requester.executeApiGet(GET_WORKSPACE_URI_TEMPLATE, null);
			try {
				Workspace workspace = new Workspace(requester);
				JacksonUtils.deserializeIntoObject(workspaceNode, workspace);
				workspacesMap.put(workspace.getKey(), workspace);
				String websocketUrl = workspace.getWorker().getWebsocketUrl();
				if (!websocketEndpointsMap.containsKey(websocketUrl)) {
					websocketEndpointsMap.put(websocketUrl, new LimooWebsocketEndpoint(workspace, limooEventListenerManager));
				}
			} catch (IOException e) {
				throw new LimooException(e);
			}
		}
	}

	@Override
	public void close() {
		for (LimooWebsocketEndpoint limooWebsocketEndpoint : websocketEndpointsMap.values()) {
			limooWebsocketEndpoint.close();
		}
	}
}
