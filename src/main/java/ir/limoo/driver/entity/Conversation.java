package ir.limoo.driver.entity;

import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ir.limoo.driver.connection.LimooRequester;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.JacksonUtils;

public class Conversation {

	private static final transient Logger logger = Logger.getLogger(Conversation.class);

	@JsonProperty("my_membership")
	private Membership membership;

	@JsonProperty("total_message_count")
	private long totalMsgCount;

	@JsonProperty("id")
	private String id;

	private static final String SEND_MSG_URI_TEMPLATE = "workspace/items/%s/conversation/items/%s/message/items";
	private static final String GET_MESSAGES_URI_TEMPLATE = "workspace/items/%s/conversation/items/%s/message/items?since=%d";
	private static final String VIEW_CONVERSATION_URI_TEMPLATE = "workspace/items/%s/conversation/items/%s/view_log";

	private Workspace workspace;
	private LimooRequester requester;

	public Conversation() {

	}

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

	/**
	 * 
	 * @param message
	 * @return The created message
	 * @throws LimooException
	 */
	public Message send(String message) throws LimooException {
		return sendInThread(message, null);
	}

	/**
	 * 
	 * @param message
	 * @param threadRootId The id of the root message of the thread
	 * @return The created message
	 * @throws LimooException
	 */
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
	
	public List<Message> getUnreadMessages() throws LimooException {
		String uri = String.format(GET_MESSAGES_URI_TEMPLATE, workspace.getId(), id, membership.lastViewedAt.getTime());
		JsonNode msgsNode = requester.executeApiGet(uri, workspace.getWorker());
		List<Message> messages = JacksonUtils.deserilizeObjectToList(msgsNode, Message.class);
		viewLog();
		return messages;
	}

	public void onNewMessage(Message message) {
		this.totalMsgCount++;
		try {
			this.viewLog();
		} catch (LimooException e) {
			logger.error(e);
		}
	}

	public void viewLog() throws LimooException {
		String uri = String.format(VIEW_CONVERSATION_URI_TEMPLATE, workspace.getId(), id);
		ObjectNode bodyNode = JacksonUtils.createEmptyObjectNode().put("prev_conversation_id", id);
		requester.executeApiPost(uri, bodyNode, workspace.getWorker());
		membership.manualView(); // TODO lastViewedAt should be updated using view_log response 
	}

	public class Membership {
		@JsonProperty("last_viewed_at")
		private Date lastViewedAt;

		@JsonProperty("msg_count")
		private long readMsgCount;

		@JsonProperty("mention_count")
		private int mentionCount;

		public Date getLastViewedAt() {
			return lastViewedAt;
		}

		public void setLastViewedAt(Date lastViewedAt) {
			this.lastViewedAt = lastViewedAt;
		}

		public long getReadMsgCount() {
			return readMsgCount;
		}

		public void setReadMsgCount(long readMsgCount) {
			this.readMsgCount = readMsgCount;
		}

		public int getMentionCount() {
			return mentionCount;
		}

		public void setMentionCount(int mentionCount) {
			this.mentionCount = mentionCount;
		}

		public void manualView() {
			this.lastViewedAt = new Date();
			this.mentionCount = 0;
			this.readMsgCount = getTotalMsgCount();
		}
	}
}
