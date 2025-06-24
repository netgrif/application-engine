package com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.runner;

import lombok.Getter;
import org.bson.types.ObjectId;

import java.io.Serial;
import java.io.Serializable;

public class Expression implements Serializable {

    @Serial
    private static final long serialVersionUID = 3687481111847498422L;

    protected ObjectId _id;

    @Getter
    protected String definition;

    public Expression() {
        this._id = new ObjectId();
    }

    public Expression(String definition) {
        this();
        this.definition = definition;
    }

    public String getStringId() {
        return _id.toString();
    }

    @Override
    public String toString() {
        return "[" + getStringId() + "] " + getDefinition();
    }

    @Override
    public Expression clone() {
        return new Expression(this.definition);
    }
}
