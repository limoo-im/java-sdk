package ir.limoo.driver.event;

import java.io.IOException;

public interface LimooEventListener {

	boolean canHandle(LimooEvent event);

	void handleEvent(LimooEvent event) throws IOException;
}
