package ir.limoo.driver.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import ir.limoo.driver.connection.LimooRequester;
import ir.limoo.driver.entity.Message;
import ir.limoo.driver.entity.MessageFile;
import ir.limoo.driver.entity.Workspace;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.exception.LimooFileUploadException;

import java.io.File;

public class MessageUtils {

	public static final String MESSAGES_ROOT_URI_TEMPLATE = "workspace/items/%s/conversation/items/%s/message/items";

	public static Message sendMessage(Message message, Workspace workspace, String conversationId) throws LimooException {
		// Upload files
		if (message.getUploadableFiles() != null) {
			for (File file : message.getUploadableFiles()) {
				try {
					JsonNode uploadedNode = LimooRequester.getInstance().uploadFile(file, workspace.getWorker());
					MessageFile fileInfo = JacksonUtils.deserializeObjectToList(uploadedNode, MessageFile.class).get(0);
					message.getCreatedFileInfos().add(fileInfo);
				} catch (Exception e) {
					throw new LimooFileUploadException(e);
				}
			}
		}

		String uri = String.format(MESSAGES_ROOT_URI_TEMPLATE, workspace.getId(), conversationId);
		JsonNode bodyNode = JacksonUtils.serializeObjectAsJsonNode(message);
		JsonNode createdMessageNode = LimooRequester.getInstance().executeApiPost(uri, bodyNode, workspace.getWorker());
		try {
			return JacksonUtils.deserializeObject(createdMessageNode, Message.class);
		} catch (JsonProcessingException e) {
			throw new LimooException(e);
		}
	}

	public static Message sendMessage(Message.Builder messageBuilder, Workspace workspace, String conversationId)
			throws LimooException {
		return sendMessage(messageBuilder.build(), workspace, conversationId);
	}
}
