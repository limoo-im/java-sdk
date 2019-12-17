package ir.limoo.driver.entity.event_listener;

import java.util.ArrayList;
import java.util.List;

import ir.limoo.driver.event.Event;

public class EventListenerManager {

	private List<EventListener> listeners;

	public EventListenerManager() {
		listeners = new ArrayList<>();
	}

	public void addToListeners(EventListener listener) {
		listeners.add(listener);
	}

	public void newEvent(Event event) {
		for (EventListener listener : listeners) {
			if (listener.canHandle(event)) {
				listener.onNewEvent(event);
			}
		}
	}
}
