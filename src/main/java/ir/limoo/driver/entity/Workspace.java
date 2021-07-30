package ir.limoo.driver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import ir.limoo.driver.connection.LimooRequester;
import ir.limoo.driver.exception.LimooException;
import ir.limoo.driver.util.JacksonUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Workspace {

    private static final String GET_CONVERSATIONS_URI_TEMPLATE = "workspace/items/%s/conversation/items";
    private static final String GET_CONVERSATION_URI_TEMPLATE = "workspace/items/%s/conversation/items/%s";

    @JsonProperty("id")
    private String id;

    @JsonProperty("name")
    private String key;

    @JsonProperty("worker_node")
    private WorkerNode worker;

    @JsonProperty("display_name")
    private String displayName;

    @JsonProperty("default_conversation_id")
    private String defaultConversationId;

    private Conversation defaultConversation;

    public String getKey() {
        return key;
    }

    public String getId() {
        return id;
    }

    public WorkerNode getWorker() {
        return worker;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultConversationId() {
        return defaultConversationId;
    }

    public Conversation getDefaultConversation() {
        if (defaultConversation == null)
            defaultConversation = new Conversation(defaultConversationId, ConversationType.PUBLIC, this);
        return defaultConversation;
    }

    public Conversation getConversationById(String conversationId) throws LimooException {
        String uri = String.format(GET_CONVERSATION_URI_TEMPLATE, getId(), conversationId);
        JsonNode conversationNode = LimooRequester.getInstance().executeApiGet(uri, worker);
        try {
            Conversation conversation = new Conversation(this);
            JacksonUtils.deserializeIntoObject(conversationNode, conversation);
            return conversation;
        } catch (IOException e) {
            throw new LimooException(e);
        }
    }

    public List<Conversation> getConversations() throws LimooException {
        String uri = String.format(GET_CONVERSATIONS_URI_TEMPLATE, getId());
        JsonNode conversationsNode = LimooRequester.getInstance().executeApiGet(uri, worker);
        try {
            ArrayNode conversationsArray = (ArrayNode) conversationsNode;
            List<Conversation> conversations = new ArrayList<>();
            for (JsonNode conversationNode : conversationsArray) {
                Conversation conversation = new Conversation(this);
                JacksonUtils.deserializeIntoObject(conversationNode, conversation);
                conversations.add(conversation);
            }
            return conversations;
        } catch (IOException e) {
            throw new LimooException(e);
        }
    }
}
