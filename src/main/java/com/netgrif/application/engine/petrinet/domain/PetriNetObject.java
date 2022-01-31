package com.netgrif.application.engine.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public abstract class PetriNetObject extends Imported {

    @Id
    @JsonIgnore
    protected ObjectId _id;

    public String getStringId() {
        return importId;
    }

    @JsonIgnore
    public ObjectId getObjectId() {
        return _id;
    }

    public void setObjectId(ObjectId _id) {
        this._id = _id;
    }
}