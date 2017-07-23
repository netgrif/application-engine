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

    protected Long id;

    public String getStringId() {
        return _id.toString();
    }

    public ObjectId getObjectId() {
        return _id;
    }

    public void setObjectId(ObjectId _id) {
        this._id = _id;
    }

    public Long getNetId() {
        return id;
    }

    public void setNetId(Long id) {
        this.id = id;
    }
}