package com.netgrif.application.engine.workflow.domain.triggers;

import com.fasterxml.jackson.annotation.JsonValue;
import com.netgrif.application.engine.petrinet.domain.Imported;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public abstract class Trigger extends Imported {

    @Id
    protected ObjectId _id;

    protected Trigger() {
        this._id = new ObjectId();
    }

    public ObjectId get_id() {
        return _id;
    }

    public void set_id(ObjectId _id) {
        this._id = _id;
    }

    public abstract Trigger clone();

    public enum Type {
        AUTO("auto"),
        MESSAGE("message"),
        TIME("time"),
        USER("user");

        String name;

        Type(String name) {
            this.name = name;
        }

        public static Type fromString(String name) {
            return Type.valueOf(name.toUpperCase());
        }

        @JsonValue
        public String getName() {
            return name;
        }
    }
}