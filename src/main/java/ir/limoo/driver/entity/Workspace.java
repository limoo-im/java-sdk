package ir.limoo.driver.entity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import ir.limoo.driver.connection.LimooRequester;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.JacksonUtils;

public class Workspace {

	@JsonProperty("worker_node")
	private WorkerNode worker;

	@JsonProperty("name")
	private String key;

	@JsonProperty("id")
	private String id;

	private LimooRequester requester;
	
	private Map<String, Conversation> conversations;

	private static final String GET_CONVERSATIONS_URI_TEMPLATE = "workspace/items/%s/conversation/items";
	private static final String GET_CONVERSATION_URI_TEMPLATE = "workspace/items/%s/conversation/items/%s";
	private static final String GET_MY_WORKSPACES_URI_TEMPLATE = "user/my_workspaces";

	public Workspace() {

	}

	public Workspace(String key, LimooRequester requester) throws LimooException {
		this.key = key;
		this.requester = requester;
		conversations = new HashMap<>();
		getAndInitWorkspace();
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public WorkerNode getWorker() {
		return worker;
	}
	
	public LimooRequester getRequester() {
		return this.requester;
	}

	private void getAndInitWorkspace() throws LimooException {
		JsonNode myWorkspacesNode = requester.executeApiGet(GET_MY_WORKSPACES_URI_TEMPLATE, null);
		List<Workspace> myWorkspaces = JacksonUtils.deserilizeObjectToList(myWorkspacesNode, Workspace.class);
		if (myWorkspaces != null) {
			boolean found = false;
			for (Workspace w : myWorkspaces) {
				if (w.getKey().equals(key)) {
					found = true;
					this.setId(w.getId());
					this.worker = w.getWorker();
					break;
				}
			}
			if (!found) {
				throw new LimooException("The provided bot isn't a member of the requested workspace.");
			}
		} else {
			throw new LimooException("The provided bot isn't a member of any workspace.");
		}
	}

	public Conversation getConversationById(String conversationId) throws LimooException {
		if (conversations.containsKey(conversationId))
			return conversations.get(conversationId);
		String uri = String.format(GET_CONVERSATION_URI_TEMPLATE, getId(), conversationId);
		JsonNode conversationNode = requester.executeApiGet(uri, worker);
		try {
			Conversation conversation = new Conversation(conversationId, this);
			JacksonUtils.deserilizeIntoObject(conversationNode, conversation);
			conversations.put(conversationId, conversation);
			return conversation;
		} catch (IOException e) {
			throw new LimooException(e);
		}
	}
	
	public List<Conversation> getConversations() throws LimooException {
		String uri = String.format(GET_CONVERSATIONS_URI_TEMPLATE, getId());
		JsonNode conversationsNode = requester.executeApiGet(uri, worker);
		try {
			ArrayNode conversationsArray = (ArrayNode) conversationsNode;
			for (JsonNode conversationNode : conversationsArray) {
				String conversationId = conversationNode.get("id").asText();
				Conversation conversation = new Conversation(conversationId, this);
				JacksonUtils.deserilizeIntoObject(conversationNode, conversation);
				conversations.put(conversationId, conversation);
			}
		} catch (IOException e) {
			throw new LimooException(e);
		}
		return new ArrayList<>(conversations.values());
	}
}
