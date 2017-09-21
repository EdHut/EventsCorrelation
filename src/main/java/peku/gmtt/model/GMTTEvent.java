package peku.gmtt.model;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "eventID",
        "eventDateTime",
        "components"
})
public class GMTTEvent {

    @JsonProperty("eventID")
    private String eventID;
    @JsonProperty("eventDateTime")
    private String eventDateTime;
    @JsonProperty("components")
    private List<Component> components = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("eventID")
    public String getEventID() {
        return eventID;
    }

    @JsonProperty("eventID")
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    @JsonProperty("eventDateTime")
    public String getEventDateTime() {
        return eventDateTime;
    }

    @JsonProperty("eventDateTime")
    public void setEventDateTime(String eventDateTime) {
        this.eventDateTime = eventDateTime;
    }

    @JsonProperty("components")
    public List<Component> getComponents() {
        return components;
    }

    @JsonProperty("components")
    public void setComponents(List<Component> components) {
        this.components = components;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
