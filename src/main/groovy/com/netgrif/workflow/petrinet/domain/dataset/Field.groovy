package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public abstract class Field<T> {

    @Id
    protected ObjectId _id;
    private String name;
    private String description;
    protected FieldType type;
    @Transient
    private ObjectNode behavior;
    @Transient
    private T value;
    private Long order
    @JsonIgnore
    private String validationRules
    @Transient
    private String validationJS
    @Transient
    private Map<String, Boolean> validationErrors

    public Field(){
        _id = new ObjectId();
    }

    public String getObjectId() {
        return _id.toString();
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
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

    public FieldType getType() {
        return type;
    }

    public void setType(FieldType type) {
        this.type = type;
    }

    ObjectNode getBehavior() {
        return behavior
    }

    void setBehavior(ObjectNode behavior) {
        this.behavior = behavior
    }

    T getValue() {
        return value
    }

    void setValue(T value) {
        this.value = value
    }

    Long getOrder() {
        return order
    }

    void setOrder(Long order) {
        this.order = order
    }

    String getValidationRules() {
        return validationRules
    }

    void setValidationRules(String validationRules) {
        this.validationRules = validationRules
    }

    void setValidationRules(String[] rules){
        StringBuilder builder = new StringBuilder()
        Arrays.stream(rules).each {rule ->
            rule = rule.trim()
            if(rule.contains(" ") || rule.contains("(")) builder.append("{${rule}},")
            else builder.append(rule+",")
        }
        builder.deleteCharAt(builder.length()-1)
        this.validationRules = builder.toString()
    }

    String getValidationJS() {
        return validationJS
    }

    void setValidationJS(String validationJS) {
        this.validationJS = validationJS
    }

    Map<String, Boolean> getValidationErrors() {
        return validationErrors
    }

    void setValidationErrors(Map<String, Boolean> validationErrors) {
        this.validationErrors = validationErrors
    }

    void addValidationError(String key, Boolean value){
        if(this.validationErrors == null) this.validationErrors = new HashMap<>()
        this.validationErrors.put(key,value)
    }

    void addValidationError(String key){
        this.addValidationError(key,false)
    }
//operators overloading
    T plus(final Field field){
        return this.value + field.value
    }

    T minus(final Field field){
        return this.value - field.value
    }

    T multiply(final Field field) {
        return this.value * field.value
    }
}
