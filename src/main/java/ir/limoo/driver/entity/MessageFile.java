package ir.limoo.driver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import ir.limoo.driver.connection.LimooRequester;
import ir.limoo.driver.exception.LimooException;

import java.io.InputStream;

public class MessageFile {
	@JsonProperty("hash")
	private String hash;

	@JsonProperty("name")
	private String name;

	@JsonProperty("size")
	private long size;

	private Workspace workspace;

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public InputStream download() throws LimooException {
		return LimooRequester.getInstance().downloadFile(hash, name, workspace.getWorker());
	}
}
