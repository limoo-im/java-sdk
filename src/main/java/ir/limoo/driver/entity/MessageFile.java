package ir.limoo.driver.entity;

import java.io.InputStream;

import com.fasterxml.jackson.annotation.JsonProperty;

import ir.limoo.driver.exception.LimooException;

public class MessageFile {
	@JsonProperty("hash")
	private String hash;

	@JsonProperty("name")
	private String name;

	@JsonProperty("mime_type")
	private String mimeType;

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

	public String getMimeType() {
		return mimeType;
	}

	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}

	public InputStream download() throws LimooException {
		return workspace.getRequester().downloadFile(hash, name, workspace.getWorker());
	}
}
