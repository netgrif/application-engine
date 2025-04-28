package com.netgrif.application.engine.objects.petrinet.domain;


import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

public class Function extends PetriNetObject {

    @Getter
    @Setter
    private String definition;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private FunctionScope scope;

    public Function() {
        this.setObjectId(new ObjectId());
        this.setImportId(this.getObjectId().toString());
    }

    public Function(Function function) {
        this.setObjectId(function.getObjectId());
        this.setImportId(function.getImportId());
        this.setDefinition(function.getDefinition());
        this.setName(function.getName());
        this.setScope(function.getScope());
    }
}
