package ir.limoo.driver;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.limoo.driver.connection.LimooRequester;
import ir.limoo.driver.connection.LimooWebsocketEndpoint;
import ir.limoo.driver.entity.Bot;
import ir.limoo.driver.entity.Workspace;
import ir.limoo.driver.event.*;
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
	private static final String JOIN_WORKSPACE_EVENT = "join_workspace";

	private final LimooEventListenerManager limooEventListenerManager;
	private final LimooRequester requester;
	private final Map<String, Workspace> workspacesMap = new HashMap<>();
	private final List<LimooWebsocketEndpoint> websocketEndpoints = new ArrayList<>();
	private Bot bot;

	public LimooDriver(String limooUrl, String botUsername, String botPassword) throws LimooException {
		limooEventListenerManager = new LimooEventListenerManager();
		requester = new LimooRequester(limooUrl, botUsername, botPassword);
		getAndInitBot();
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
				workspacesMap.put(workspace.getId(), workspace);
                websocketEndpoints.add(new LimooWebsocketEndpoint(workspace, limooEventListenerManager, this));
			}
		} catch (IOException e) {
			throw new LimooException("An error occurred while getting bot's workspaces.");
		}
	}

	private void handleJoinWorkspaceEvent() {
		limooEventListenerManager.addToListeners(new LimooEventListener() {
			@Override
			public boolean canHandle(LimooEvent event) {
				return JOIN_WORKSPACE_EVENT.equals(event.getType())
						&& event.getEventData().has("user_id")
						&& event.getEventData().has("workspace_id");
			}

			@Override
			public void handleEvent(LimooEvent event) throws IOException {
				JsonNode dataNode = event.getEventData();
				String userId = dataNode.get("user_id").asText();
				if (userId.equals(bot.getId())) {
					try {
						String workspaceId = dataNode.get("workspace_id").asText();
                        if (!workspacesMap.containsKey(workspaceId)) {
						    Workspace addedWorkspace = getWorkspaceById(workspaceId);
							limooEventListenerManager.newEvent(new LimooEvent(
									AddedToWorkspaceEventListener.ADDED_TO_WORKSPACE,
									event.getEventData(),
									addedWorkspace
							));
						}
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

	public Workspace getWorkspaceById(String workspaceId) throws LimooException {
        if (workspacesMap.containsKey(workspaceId))
            return workspacesMap.get(workspaceId);

        JsonNode workspaceNode = requester.executeApiGet(GET_WORKSPACE_URI_TEMPLATE, null);
        try {
            Workspace workspace = new Workspace(requester);
            JacksonUtils.deserializeIntoObject(workspaceNode, workspace);
            workspacesMap.put(workspace.getKey(), workspace);
            websocketEndpoints.add(new LimooWebsocketEndpoint(workspace, limooEventListenerManager, this));
            return workspace;
        } catch (IOException e) {
            throw new LimooException(e);
        }
	}

	public Workspace getWorkspaceByKey(String workspaceKey) {
		return workspacesMap.values().stream()
                .filter(w -> w.getKey().equals(workspaceKey))
                .findAny().orElse(null);
	}

	@Override
	public void close() {
		for (LimooWebsocketEndpoint limooWebsocketEndpoint : websocketEndpoints) {
			limooWebsocketEndpoint.close();
		}
	}
}
