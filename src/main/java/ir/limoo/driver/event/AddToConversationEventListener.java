package ir.limoo.driver.event;

import com.fasterxml.jackson.databind.JsonNode;
import ir.limoo.driver.entity.Conversation;
import ir.limoo.driver.entity.ConversationType;

public abstract class AddToConversationEventListener implements LimooEventListener {

	private static final String ADDED_TO_CONVERSATION_EVENT = "added_to_conversation";

	public AddToConversationEventListener() {
	}

	public abstract void onAddToConversation(Conversation conversation);

	@Override
	public boolean canHandle(LimooEvent event) {
		return ADDED_TO_CONVERSATION_EVENT.equals(event.getType()) && event.getEventData().has("id");
	}

	@Override
	public void handleEvent(LimooEvent event) {
		JsonNode dataNode = event.getEventData();
		String id = dataNode.get("id").asText();
		String conversationTypeStr = dataNode.get("type").asText();
		ConversationType type = ConversationType.valueOfLabel(conversationTypeStr);
		Conversation conversation = new Conversation(id, type, event.getWorkspace());
		onAddToConversation(conversation);
	}
}
