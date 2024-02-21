package ir.limoo.driver.event;

import com.fasterxml.jackson.databind.JsonNode;
import ir.limoo.driver.entity.Conversation;
import ir.limoo.driver.entity.ConversationType;
import ir.limoo.driver.entity.Message;
import ir.limoo.driver.util.JacksonUtils;

import java.io.IOException;

public abstract class MessageCreatedEventListener implements LimooEventListener {

	private static final String MESSAGE_CREATED_EVENT = "message_created";

	public MessageCreatedEventListener() {
	}

	public abstract void onNewMessage(Message message, Conversation conversation);

	@Override
	public boolean canHandle(LimooEvent event) {
		return MESSAGE_CREATED_EVENT.equals(event.getType()) && event.getEventData().has("message");
	}

	@Override
	public void handleEvent(LimooEvent event) throws IOException {
		JsonNode dataNode = event.getEventData();
		JsonNode messageNode = dataNode.get("message");
		Message message = new Message(event.getWorkspace());
		JacksonUtils.deserializeIntoObject(messageNode, message);
		ConversationType type = ConversationType.valueOfLabel(dataNode.get("conversation_type").asText());
		Conversation conversation = new Conversation(message.getConversationId(), type, event.getWorkspace());
		onNewMessage(message, conversation);
		conversation.onNewMessage();
	}
}
