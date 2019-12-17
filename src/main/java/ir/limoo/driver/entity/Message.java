package ir.limoo.driver.entity;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Message {

	@JsonProperty("text")
	private String text;

	@JsonProperty("user_id")
	private String userId;
	
	@JsonProperty("create_at")
	private Date createAt;
	
	@JsonProperty("conversation_id")
	private String conversationId;

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
}
