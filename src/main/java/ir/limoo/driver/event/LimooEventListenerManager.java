package ir.limoo.driver.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LimooEventListenerManager {

	private static final Logger logger = LoggerFactory.getLogger(LimooEventListenerManager.class);

	private final List<LimooEventListener> listeners;

	public LimooEventListenerManager() {
		listeners = new ArrayList<>();
	}

	public void addToListeners(LimooEventListener listener) {
		listeners.add(listener);
	}

	public void removeFromListeners(LimooEventListener listener) {
		listeners.remove(listener);
	}

	public void newEvent(LimooEvent event) {
		for (LimooEventListener listener : listeners) {
			if (listener.canHandle(event)) {
				try {
					listener.handleEvent(event);
				} catch (IOException e) {
					logger.error("", e);
				}
			}
		}
	}
}
