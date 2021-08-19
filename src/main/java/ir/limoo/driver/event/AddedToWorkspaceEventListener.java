package ir.limoo.driver.event;

import ir.limoo.driver.entity.Workspace;

public abstract class AddedToWorkspaceEventListener implements LimooEventListener {

	public static final String ADDED_TO_WORKSPACE = "added_to_workspace";

	public AddedToWorkspaceEventListener() {
	}

	public abstract void onAddToWorkspace(Workspace workspace);

	@Override
	public boolean canHandle(LimooEvent event) {
		return ADDED_TO_WORKSPACE.equals(event.getType());
	}

	@Override
	public void handleEvent(LimooEvent event) {
		onAddToWorkspace(event.getWorkspace());
	}
}
