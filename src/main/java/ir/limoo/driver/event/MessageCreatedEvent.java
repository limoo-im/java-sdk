package ir.limoo.driver.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;

import ir.limoo.driver.entity.Message;
import ir.limoo.driver.util.JacksonUtils;

public class MessageCreatedEvent implements Event {

	private Message message;

	public MessageCreatedEvent(JsonNode dataNode) throws JsonProcessingException {
		JsonNode msgNode = dataNode.get("message");
		Message msg = JacksonUtils.deserilizeObject(msgNode, Message.class);
		this.message = msg;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

}
