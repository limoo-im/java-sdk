package ir.limoo.driver.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.MessageUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Message {

	@JsonProperty("id")
	@JsonInclude(Include.NON_NULL)
	private String id;

	@JsonProperty("text")
	private String text = "";

	@JsonProperty("user_id")
	@JsonInclude(Include.NON_NULL)
	private String userId;

	@JsonProperty("create_at")
	@JsonInclude(Include.NON_NULL)
	private Date createAt;

	@JsonProperty("conversation_id")
	@JsonInclude(Include.NON_NULL)
	private String conversationId;

	@JsonProperty("thread_root_id")
	@JsonInclude(Include.NON_NULL)
	private String threadRootId;

	@JsonProperty("files")
	@JsonInclude(Include.NON_NULL)
	private List<MessageFile> fileInfos;

	@JsonProperty("direct_reply_message_id")
	@JsonInclude(Include.NON_NULL)
	private String directReplyMessageId;

	private List<File> files;
	private Workspace workspace;

	public Message() {
	}

	public Message(Workspace workspace) {
		this.workspace = workspace;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Date getCreateAt() {
		return createAt;
	}

	public void setCreateAt(Date createAt) {
		this.createAt = createAt;
	}

	public String getConversationId() {
		return conversationId;
	}

	public void setConversationId(String conversationId) {
		this.conversationId = conversationId;
	}

	public String getThreadRootId() {
		return threadRootId;
	}

	public void setThreadRootId(String threadRootId) {
		this.threadRootId = threadRootId;
	}

	public List<MessageFile> getCreatedFileInfos() {
		if (this.fileInfos == null)
			this.fileInfos = new ArrayList<>();
		return this.fileInfos;
	}

	public List<MessageFile> getFileInfos() {
		return fileInfos;
	}

	public void setFileInfos(List<MessageFile> fileInfos) {
		this.fileInfos = fileInfos;
		if (fileInfos != null) {
			for (MessageFile file : fileInfos) {
				file.setWorkspace(workspace);
			}
		}
	}

	public String getDirectReplyMessageId() {
		return directReplyMessageId;
	}

	public void setDirectReplyMessageId(String directReplyMessageId) {
		this.directReplyMessageId = directReplyMessageId;
	}

	public List<File> getUploadableFiles() {
		return files;
	}

	public void addFile(File file) {
		if (this.files == null)
			this.files = new ArrayList<>();
		this.files.add(file);
	}

	public void addFiles(List<File> files) {
		if (this.files == null)
			this.files = new ArrayList<>();
		this.files.addAll(files);
	}

	public Workspace getWorkspace() {
		return workspace;
	}

	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
		if (fileInfos != null) {
			for (MessageFile file : fileInfos) {
				file.setWorkspace(workspace);
			}
		}
	}

	public Message sendInThread(String text) throws LimooException {
		String threadId = threadRootId == null ? id : threadRootId;
		Message.Builder messageBuilder = new Message.Builder().text(text).threadRootId(threadId);
		return MessageUtils.sendMessage(messageBuilder, workspace, conversationId);
	}

	public Message sendInThread(Message.Builder messageBuilder) throws LimooException {
		String threadId = threadRootId == null ? id : threadRootId;
		messageBuilder.threadRootId(threadId);
		return MessageUtils.sendMessage(messageBuilder, workspace, conversationId);
	}

	public static class Builder {
		Message message;

		public Builder() {
			this.message = new Message();
		}

		public Message build() {
			return message;
		}

		public Builder text(String text) {
			message.text = text;
			return this;
		}

		public Builder threadRootId(String threadRootId) {
			message.threadRootId = threadRootId;
			return this;
		}

		public Builder fileInfo(MessageFile fileInfo) {
			message.getCreatedFileInfos().add(fileInfo);
			return this;
		}

		public Builder fileInfos(List<MessageFile> fileInfos) {
			message.getCreatedFileInfos().addAll(fileInfos);
			return this;
		}

		public Builder file(File file) {
			message.addFile(file);
			return this;
		}

		public Builder files(List<File> files) {
			message.addFiles(files);
			return this;
		}
	}

}
