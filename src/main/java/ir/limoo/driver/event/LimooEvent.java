package ir.limoo.driver.event;

import com.fasterxml.jackson.databind.JsonNode;
import ir.limoo.driver.entity.Workspace;

public class LimooEvent {

	private String type;
	private JsonNode eventData;
	private Workspace workspace;

	public LimooEvent(String type, JsonNode eventData, Workspace workspace) {
		this.type = type;
		this.eventData = eventData;
		this.workspace = workspace;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public JsonNode getEventData() {
		return eventData;
	}

	public void setEventData(JsonNode eventData) {
		this.eventData = eventData;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
}
