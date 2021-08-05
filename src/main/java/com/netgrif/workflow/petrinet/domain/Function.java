package com.netgrif.workflow.petrinet.domain;


import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Function extends PetriNetObject {

    @Getter
    @Setter
    private String definition;

    @Getter
    @Setter
    private String name;

    private boolean _static;

    public boolean isStatic() {
        return _static;
    }

    public void setStatic(boolean value) {
        this._static = value;
    }

    public Function() {
        this.setObjectId(new ObjectId());
    }

    public Function clone() {
        Function clone = new Function();
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        clone.setDefinition(this.definition);
        clone.setName(this.name);
        clone.setStatic(this._static);
        return clone;
    }
}
