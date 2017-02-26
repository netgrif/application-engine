package com.fmworkflow.petrinet.domain.dataset;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public abstract class Field {

    @Id
    protected ObjectId _id;
    private String name;
    private String description;
    @Transient
    protected FieldType type;
    @Transient
    private ObjectNode logic;

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

    public ObjectNode getLogic() {
        return logic;
    }

    public void setLogic(ObjectNode logic) {
        this.logic = logic;
    }

    public void setValue(Object value){}
}
