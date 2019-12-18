package ir.limoo.driver.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {

	@JsonProperty("id")
	private String id;

	@JsonProperty("text")
	private String text;

	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("create_at")
	private Date createAt;
	
	@JsonProperty("conversation_id")
	private String conversationId;

	@JsonProperty("thread_root_id")
	private String threadRootId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getThreadRootId() {
		return threadRootId;
	}

	public void setThreadRootId(String threadRootId) {
		this.threadRootId = threadRootId;
	}
}
