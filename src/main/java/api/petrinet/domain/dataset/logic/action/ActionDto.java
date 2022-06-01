package api.petrinet.domain.dataset.logic.action;

import java.util.HashMap;
import java.util.Map;

public class ActionDto {

    private String importId;

    private String id;

    private Map<String, String> fieldIds = new HashMap<>();

    private Map<String, String> transitionIds = new HashMap<>();

    private String definition;

    private String trigger;

    public ActionDto() {
    }

    public ActionDto(String importId, String id, Map<String, String> fieldIds, Map<String, String> transitionIds, String definition, String trigger) {
        this.importId = importId;
        this.id = id;
        this.fieldIds = fieldIds;
        this.transitionIds = transitionIds;
        this.definition = definition;
        this.trigger = trigger;
    }

    public String getImportId() {
        return importId;
    }

    public Map<String, String> getFieldIds() {
        return fieldIds;
    }

    public Map<String, String> getTransitionIds() {
        return transitionIds;
    }

    public String getDefinition() {
        return definition;
    }

    public String getTrigger() {
        return trigger;
    }

    public void setImportId(String importId) {
        this.importId = importId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setFieldIds(Map<String, String> fieldIds) {
        this.fieldIds = fieldIds;
    }

    public void setTransitionIds(Map<String, String> transitionIds) {
        this.transitionIds = transitionIds;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public void setTrigger(String trigger) {
        this.trigger = trigger;
    }
}
