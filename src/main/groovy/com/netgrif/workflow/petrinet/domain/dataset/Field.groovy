package com.netgrif.workflow.petrinet.domain.dataset

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.node.ObjectNode
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.Transient
import org.springframework.data.mongodb.core.mapping.Document

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
    private Boolean immediate

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

    Boolean isImmediate() {
        return immediate
    }

    void setImmediate(Boolean immediate) {
        this.immediate = immediate
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
