package ir.limoo.driver.event;

import java.io.IOException;

import com.fasterxml.jackson.databind.JsonNode;

import ir.limoo.driver.entity.Message;
import ir.limoo.driver.entity.Workspace;
import ir.limoo.driver.util.JacksonUtils;

public class MessageCreatedEvent implements Event {

	private Message message;

	public MessageCreatedEvent(Workspace workspace, JsonNode dataNode) throws IOException {
		JsonNode msgNode = dataNode.get("message");
		Message msg = new Message(workspace);
		JacksonUtils.deserilizeIntoObject(msgNode, msg);
		this.message = msg;
	}

	public Message getMessage() {
		return message;
	}

	public void setMessage(Message message) {
		this.message = message;
	}

}
