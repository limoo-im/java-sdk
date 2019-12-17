package ir.limoo.driver.entity.event_listener;

import ir.limoo.driver.event.Event;

public interface EventListener {

	public boolean canHandle(Event event);
	public void onNewEvent(Event event);

}
