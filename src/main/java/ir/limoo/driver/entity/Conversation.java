package ir.limoo.driver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.JacksonUtils;
import ir.limoo.driver.util.MessageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Conversation {

	private static final Logger logger = LoggerFactory.getLogger(Conversation.class);

	private static final String GET_MESSAGES_URI_TEMPLATE = "workspace/items/%s/conversation/items/%s/message/items?since=%d";
	private static final String VIEW_CONVERSATION_URI_TEMPLATE = "workspace/items/%s/conversation/items/%s/view_log";

	@JsonProperty("my_membership")
	private Membership membership;

	@JsonProperty("total_message_count")
	private long totalMsgCount;

	@JsonProperty("id")
	private String id;

    @JsonProperty("display_name")
    private String displayName;

	private ConversationType conversationType;
	private Workspace workspace;

	public Conversation() {
	}

	public Conversation(Workspace workspace) {
		this.workspace = workspace;
	}

	public Conversation(String id, ConversationType conversationType, Workspace workspace) {
		this.id = id;
		this.conversationType = conversationType;
		this.workspace = workspace;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

	public ConversationType getConversationType() {
		return conversationType;
	}

	public void setConversationType(ConversationType conversationType) {
		this.conversationType = conversationType;
	}

	public Workspace getWorkspace() {
		return this.workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public Message send(String message) throws LimooException {
		return send(new Message.Builder().text(message));
	}

	public Message send(Message.Builder builder) throws LimooException {
		return MessageUtils.sendMessage(builder, workspace, id);
	}

	public List<Message> getUnreadMessages() throws LimooException {
		String uri = String.format(GET_MESSAGES_URI_TEMPLATE, workspace.getId(), id, membership.lastViewedAt.getTime());
		ArrayNode messagesNode = (ArrayNode) workspace.getRequester().executeApiGet(uri, workspace.getWorker());
		List<Message> messages = new ArrayList<>();
		for (JsonNode messageNode : messagesNode) {
			Message message = new Message(workspace);
			try {
				JacksonUtils.deserializeIntoObject(messageNode, message);
				messages.add(message);
			} catch (IOException e) {
				throw new LimooException(e);
			}
		}
		viewLog();
		return messages;
	}

	public void onNewMessage() {
		this.totalMsgCount++;
		this.viewLog();
	}

	public void viewLog() {
		String uri = String.format(VIEW_CONVERSATION_URI_TEMPLATE, workspace.getId(), id);
		ObjectNode bodyNode = JacksonUtils.createEmptyObjectNode().put("prev_conversation_id", id);
		try {
			JsonNode resNode = workspace.getRequester().executeApiPost(uri, bodyNode, workspace.getWorker());
			if (membership != null) {
				JsonNode lastViewedAtTimes = resNode.get("last_viewed_at_times");
				Date lastViewedAt = new Date();
				if (lastViewedAtTimes.has(id))
					lastViewedAt = new Date(lastViewedAtTimes.get(id).asLong());
				membership.manualView(lastViewedAt, getTotalMsgCount());
			}
		} catch (LimooException e) {
			logger.error("", e);
		}
	}

	public static class Membership {
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

		public void manualView(Date lastViewedAt, long totalMsgCount) {
			this.lastViewedAt = lastViewedAt;
			this.mentionCount = 0;
			this.readMsgCount = totalMsgCount;
		}
	}
}
