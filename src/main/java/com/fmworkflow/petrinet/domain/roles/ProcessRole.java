package com.fmworkflow.petrinet.domain.roles;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ProcessRole {
    @Id
    private ObjectId _id;
    private String name;
    private String description;

    public ProcessRole() {
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

    public String getStringId() {
        return _id.toString();
    }
}