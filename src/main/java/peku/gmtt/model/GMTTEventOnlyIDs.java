package peku.gmtt.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "componentId",
        "localComponentId",
        "externalComponentId",
        "componentProperties",
        "eventId"
})

public class GMTTEventOnlyIDs {
    @JsonProperty("componentId")
    private String componentId;
    @JsonProperty("localComponentId")
    private String localComponentId;
    @JsonProperty("externalComponentId")
    private String externalComponentId;
    @JsonProperty("eventId")
    private String eventId;

    @JsonProperty("componentId")
    public String getComponentId() {
        return componentId;
    }

    @JsonProperty("componentId")
    public void setComponentId(String componentId) {
        this.componentId = componentId;
    }

    @JsonProperty("localComponentId")
    public String getLocalComponentId() {
        return localComponentId;
    }

    @JsonProperty("localComponentId")
    public void setLocalComponentId(String localComponentId) {
        this.localComponentId = localComponentId;
    }

    @JsonProperty("externalComponentId")
    public String getExternalComponentId() {
        return externalComponentId;
    }

    @JsonProperty("externalComponentId")
    public void setExternalComponentId(String externalComponentId) {
        this.externalComponentId = externalComponentId;
    }

    @JsonProperty("eventId")
    public String getEventId() {
        return eventId;
    }

    @JsonProperty("eventId")
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    @Override
    public String toString() {
        return "Event{" +
                "ComponentId='" + this.componentId + '\'' +
                "LocalComponentId='" + this.localComponentId + '\'' +
                "ExternalComponentId='" + this.externalComponentId + '\'' +
                "EventId='" + this.eventId + '\'' +
                '}';
    }

}
