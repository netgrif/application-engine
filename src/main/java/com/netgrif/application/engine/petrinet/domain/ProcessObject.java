package com.netgrif.application.engine.petrinet.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@EqualsAndHashCode(callSuper = true)
@Data
@Document
public abstract class ProcessObject extends Imported {

    @Id
    @JsonIgnore
    protected ObjectId id;

    public ProcessObject() {
        this.id = new ObjectId();
    }

    public String getStringId() {
        return importId;
    }

    @JsonIgnore
    public ObjectId getObjectId() {
        return id;
    }

    public void setObjectId(ObjectId id) {
        this.id = id;
    }
}