package ir.limoo.driver.event;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class JoinWorkspaceEventListener implements LimooEventListener {

	private static final String JOIN_WORKSPACE = "join_workspace";

	public JoinWorkspaceEventListener() {
	}

	public abstract void onJoinWorkspace(String userId, String workspaceId);

	@Override
	public boolean canHandle(LimooEvent event) {
		return JOIN_WORKSPACE.equals(event.getType())
				&& event.getEventData().has("user_id")
				&& event.getEventData().has("workspace_id");
	}

	@Override
	public void handleEvent(LimooEvent event) {
		JsonNode dataNode = event.getEventData();
		String userId = dataNode.get("user_id").asText();
		String workspaceId = dataNode.get("workspace_id").asText();
		onJoinWorkspace(userId, workspaceId);
	}
}
