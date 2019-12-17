package ir.limoo.driver.entity.event_listener;

import ir.limoo.driver.entity.Conversation;
import ir.limoo.driver.entity.Message;
import ir.limoo.driver.event.Event;
import ir.limoo.driver.event.MessageCreatedEvent;

public abstract class MessageCreatedEventListener implements EventListener {

	public abstract void onNewMessage(Message message);

	private Conversation conversation;

	public MessageCreatedEventListener(Conversation conversation) {
		this.conversation = conversation;
	}

	@Override
	public boolean canHandle(Event event) {
		if (event instanceof MessageCreatedEvent) {
			MessageCreatedEvent messageCreatedEvent = (MessageCreatedEvent) event;
			String conversationId = messageCreatedEvent.getMessage().getConversationId();
			if (conversation.getId().equals(conversationId))
				return true;
		}
		return false;
	}

	@Override
	public void onNewEvent(Event event) {
		MessageCreatedEvent messageCreatedEvent = (MessageCreatedEvent) event;
		onNewMessage(messageCreatedEvent.getMessage());
	}
}
