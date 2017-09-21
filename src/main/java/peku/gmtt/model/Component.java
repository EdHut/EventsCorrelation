package peku.gmtt.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang.builder.ToStringBuilder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "localComponentID",
        "componentID",
        "externalComponentID",
        "componentStatus",
        "componentStatusDateChangeTime",
        "destinationName",
        "applicationName",
        "buildingBlockName",
        "hostName",
        "componentProperties"
})
public class Component {

    @JsonProperty("localComponentID")
    private String localComponentID;
    @JsonProperty("externalComponentID")
    private String externalComponentID;
    @JsonProperty("componentID")
    private String componentID;
    @JsonProperty("componentStatus")
    private String componentStatus;
    @JsonProperty("componentStatusDateChangeTime")
    private String componentStatusDateChangeTime;
    @JsonProperty("destinationName")
    private String destinationName;
    @JsonProperty("applicationName")
    private String applicationName;
    @JsonProperty("buildingBlockName")
    private String buildingBlockName;
    @JsonProperty("hostName")
    private String hostName;
    @JsonProperty("componentProperties")
    private List<ComponentProperty> componentProperties = null;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonProperty("localComponentID")
    public String getLocalComponentID() {
        return localComponentID;
    }

    @JsonProperty("localComponentID")
    public void setLocalComponentID(String localComponentID) {
        this.localComponentID = localComponentID;
    }

    @JsonProperty("externalComponentID")
    public String getExternalComponentID() {
        return externalComponentID;
    }

    @JsonProperty("externalComponentID")
    public void setExternalComponentID(String externalComponentID) {
        this.externalComponentID = externalComponentID;
    }

    @JsonProperty("componentID")
    public String getComponentID() {
        return componentID;
    }

    @JsonProperty("componentID")
    public void setComponentID(String componentID) {
        this.componentID = componentID;
    }

    @JsonProperty("componentStatus")
    public String getComponentStatus() {
        return componentStatus;
    }

    @JsonProperty("componentStatus")
    public void setComponentStatus(String componentStatus) {
        this.componentStatus = componentStatus;
    }

    @JsonProperty("componentStatusDateChangeTime")
    public String getComponentStatusDateChangeTime() {
        return componentStatusDateChangeTime;
    }

    @JsonProperty("componentStatusDateChangeTime")
    public void setComponentStatusDateChangeTime(String componentStatusDateChangeTime) {
        this.componentStatusDateChangeTime = componentStatusDateChangeTime;
    }

    @JsonProperty("destinationName")
    public String getDestinationName() {
        return destinationName;
    }

    @JsonProperty("destinationName")
    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    @JsonProperty("applicationName")
    public String getApplicationName() {
        return applicationName;
    }

    @JsonProperty("applicationName")
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    @JsonProperty("buildingBlockName")
    public String getBuildingBlockName() {
        return buildingBlockName;
    }

    @JsonProperty("buildingBlockName")
    public void setBuildingBlockName(String buildingBlockName) {
        this.buildingBlockName = buildingBlockName;
    }

    @JsonProperty("hostName")
    public String getHostName() {
        return hostName;
    }

    @JsonProperty("hostName")
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    @JsonProperty("componentProperties")
    public List<ComponentProperty> getComponentProperties() {
        return componentProperties;
    }

    @JsonProperty("componentProperties")
    public void setComponentProperties(List<ComponentProperty> componentProperties) {
        this.componentProperties = componentProperties;
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