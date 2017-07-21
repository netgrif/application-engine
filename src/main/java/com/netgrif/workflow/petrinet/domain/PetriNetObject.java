package com.netgrif.workflow.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public abstract class PetriNetObject {

    @Id
    @JsonIgnore
    protected ObjectId _id;

    protected Integer id;

    public String getStringId() {
        return _id.toString();
    }

    public ObjectId getObjectId() {
        return _id;
    }

    public void setObjectId(ObjectId _id) {
        this._id = _id;
    }

    public Integer getNetId() {
        return id;
    }

    public void setNetId(Integer id) {
        this.id = id;
    }
}