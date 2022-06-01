package api.petrinet.domain.dataset;

import api.petrinet.domain.ComponentDto;
import api.petrinet.domain.I18nStringDto;
import api.petrinet.domain.ImportedDto;
import api.petrinet.domain.dataset.logic.FieldLayoutDto;
import api.petrinet.domain.dataset.logic.action.runner.ExpressionDto;
import api.petrinet.domain.dataset.logic.validation.ValidationDto;
import api.petrinet.domain.events.DataEventDto;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;
import java.util.Map;

public abstract class FieldDto<T> extends ImportedDto {

    private String id;

    private String type;

    private I18nStringDto name;

    private I18nStringDto description;

    private I18nStringDto placeholder;

    private ObjectNode behavior;

    private FieldLayoutDto layout;

    private T value;

    private Long order;

    private boolean immediate;

    private Map<String, DataEventDto> events;

    private String encryption;

    private Integer length;

    private ComponentDto component;

    private T defaultValue;

    private ExpressionDto initExpression;

    private List<ValidationDto> validations;

    private String parentTaskId;

    private String parentCaseId;

    public FieldDto() {
    }

    public FieldDto(String id, String type, I18nStringDto name, I18nStringDto description, I18nStringDto placeholder, ObjectNode behavior, FieldLayoutDto layout, T value, Long order, boolean immediate, Map<String, DataEventDto> events, String encryption, Integer length, ComponentDto component, T defaultValue, ExpressionDto initExpression, List<ValidationDto> validations, String parentTaskId, String parentCaseId) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.description = description;
        this.placeholder = placeholder;
        this.behavior = behavior;
        this.layout = layout;
        this.value = value;
        this.order = order;
        this.immediate = immediate;
        this.events = events;
        this.encryption = encryption;
        this.length = length;
        this.component = component;
        this.defaultValue = defaultValue;
        this.initExpression = initExpression;
        this.validations = validations;
        this.parentTaskId = parentTaskId;
        this.parentCaseId = parentCaseId;
    }

    public String getStringId() {
        return id;
    }

    public void setStringId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public I18nStringDto getName() {
        return name;
    }

    public void setName(I18nStringDto name) {
        this.name = name;
    }

    public I18nStringDto getDescription() {
        return description;
    }

    public void setDescription(I18nStringDto description) {
        this.description = description;
    }

    public I18nStringDto getPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(I18nStringDto placeholder) {
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

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public Long getOrder() {
        return order;
    }

    public void setOrder(Long order) {
        this.order = order;
    }

    public boolean isImmediate() {
        return immediate;
    }

    public void setImmediate(boolean immediate) {
        this.immediate = immediate;
    }

    public Map<String, DataEventDto> getEvents() {
        return events;
    }

    public void setEvents(Map<String, DataEventDto> events) {
        this.events = events;
    }

    public String getEncryption() {
        return encryption;
    }

    public void setEncryption(String encryption) {
        this.encryption = encryption;
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

    public T getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(T defaultValue) {
        this.defaultValue = defaultValue;
    }

    public ExpressionDto getInitExpression() {
        return initExpression;
    }

    public void setInitExpression(ExpressionDto initExpression) {
        this.initExpression = initExpression;
    }

    public List<ValidationDto> getValidations() {
        return validations;
    }

    public void setValidations(List<ValidationDto> validations) {
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
