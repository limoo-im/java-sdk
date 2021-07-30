package ir.limoo.driver.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

public class WorkerNode {

    @JsonProperty("api_url")
    private String apiUrl;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("websocket_url")
    private String websocketUrl;

    public String getApiUrl() {
        return apiUrl;
    }

    public void setApiUrl(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getWebsocketUrl() {
        return websocketUrl;
    }

    public void setWebsocketUrl(String websocketUrl) {
        this.websocketUrl = websocketUrl;
    }

}
