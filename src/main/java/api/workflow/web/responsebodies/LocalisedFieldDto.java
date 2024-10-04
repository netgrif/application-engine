package api.workflow.web.responsebodies;

import com.fasterxml.jackson.databind.node.ObjectNode;
import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;
import api.petrinet.domain.dataset.logic.validation.LocalisedValidationDto;

import java.util.List;

public abstract class LocalisedFieldDto {

    private String stringId;

    private String type;

    private String name;

    private String description;

    private String placeholder;

    private ObjectNode behavior;

    private FieldLayoutDto layout;

    private Object value;

    private Long order;

    private Integer length;

    private ComponentDto component;

    private List<LocalisedValidationDto> validations;

    private String parentTaskId;

    private String parentCaseId;

    public LocalisedFieldDto() {
    }

    public LocalisedFieldDto(String stringId, String type, String name, String description, String placeholder, ObjectNode behavior, FieldLayoutDto layout, Object value, Long order, Integer length, ComponentDto component, List<LocalisedValidationDto> validations, String parentTaskId, String parentCaseId) {
        this.stringId = stringId;
        this.type = type;
        this.name = name;
        this.description = description;
        this.placeholder = placeholder;
        this.behavior = behavior;
        this.layout = layout;
        this.value = value;
        this.order = order;
        this.length = length;
        this.component = component;
        this.validations = validations;
        this.parentTaskId = parentTaskId;
        this.parentCaseId = parentCaseId;
    }

    public String getStringId() {
        return stringId;
    }

    public void setStringId(String stringId) {
        this.stringId = stringId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    public ObjectNode getBehavior() {
        return behavior;
    }

    public void setBehavior(ObjectNode behavior) {
        this.behavior = behavior;
    }

    public FieldLayoutDto getLayout() {
        return layout;
    }

    public void setLayout(FieldLayoutDto layout) {
        this.layout = layout;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public ComponentDto getComponent() {
        return component;
    }

    public void setComponent(ComponentDto component) {
        this.component = component;
    }

    public List<LocalisedValidationDto> getValidations() {
        return validations;
    }

    public void setValidations(List<LocalisedValidationDto> validations) {
        this.validations = validations;
    }

    public String getParentTaskId() {
        return parentTaskId;
    }

    public void setParentTaskId(String parentTaskId) {
        this.parentTaskId = parentTaskId;
    }

    public String getParentCaseId() {
        return parentCaseId;
    }

    public void setParentCaseId(String parentCaseId) {
        this.parentCaseId = parentCaseId;
    }
}
