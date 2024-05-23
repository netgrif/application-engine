package com.netgrif.application.engine.petrinet.domain.dataset.logic;

import lombok.Data;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
public class Expression implements Serializable {

    private static final long serialVersionUID = 3687481111847498422L;

    private String id;
    private String definition;
    private boolean dynamic;

    public Expression(String definition, boolean dynamic) {
        this.id = new ObjectId().toString();
        this.definition = definition;
        this.dynamic = dynamic;
    }

    @Override
    public String toString() {
        return definition;
    }

    @Override
    public Expression clone() {
        return new Expression(this.definition, this.dynamic);
    }
}
