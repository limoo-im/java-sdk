package ir.limoo.driver.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ir.limoo.driver.connection.LimooRequester;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.JacksonUtils;

public class Conversation {

	@JsonProperty("my_membership")
	private Membership membership;
	
	@JsonProperty("total_message_count")
	private long totalMsgCount;

	@JsonProperty("id")
	private String id;

	private static final String SEND_MSG_URI_TEMPLATE = "workspace/items/%s/conversation/items/%s/message/items";
	
	private Workspace workspace;
	private LimooRequester requester;
	
	public Conversation(String id, Workspace workspace, LimooRequester requester) {
		this.id = id;
		this.workspace = workspace;
		this.requester = requester;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Membership getMembership() {
		return membership;
	}

	public void setMembership(Membership membership) {
		this.membership = membership;
	}

	public long getTotalMsgCount() {
		return totalMsgCount;
	}

	public void setTotalMsgCount(long totalMsgCount) {
		this.totalMsgCount = totalMsgCount;
	}

	public Message send(String message) throws LimooException {
		return sendInThread(message, null);
	}

	public Message sendInThread(String message, String threadRootId) throws LimooException {
		String uri = String.format(SEND_MSG_URI_TEMPLATE, workspace.getId(), id);
		ObjectNode bodyNode = JacksonUtils.createEmptyObjectNode().put("text", message);
		if (threadRootId != null)
			bodyNode.put("thread_root_id", threadRootId);
		JsonNode createdMsgNode = requester.executeApiPost(uri, bodyNode, workspace.getWorker());
		try {
			return JacksonUtils.deserilizeObject(createdMsgNode, Message.class);
		} catch (JsonProcessingException e) {
			throw new LimooException(e);
		}
	}

	class Membership {
		@JsonProperty("last_viewed_at")
		private Date lastViewedAt;

		@JsonProperty("msg_count")
		private long readMsgCount;

		@JsonProperty("mention_count")
		private int mentionCount;
	}
}
