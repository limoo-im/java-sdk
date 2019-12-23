package ir.limoo.driver.entity;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;

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

	public Conversation getConversationById(String conversatinoId) throws LimooException {
		if (conversations.containsKey(conversatinoId))
			return conversations.get(conversatinoId);
		String uri = String.format(GET_CONVERSATION_URI_TEMPLATE, getId(), conversatinoId);
		JsonNode conversationsNode = requester.executeApiGet(uri, worker);
		try {
			Conversation conversation = new Conversation(conversatinoId, this);
			JacksonUtils.deserilizeIntoObject(conversationsNode, conversation);
			conversations.put(conversatinoId, conversation);
			return conversation;
		} catch (IOException e) {
			throw new LimooException(e);
		}
	}
}
