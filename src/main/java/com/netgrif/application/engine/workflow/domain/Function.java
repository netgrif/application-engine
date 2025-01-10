package com.netgrif.application.engine.workflow.domain;


import lombok.Getter;
import lombok.Setter;
import com.netgrif.application.engine.importer.model.Scope;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class Function extends CaseElement {

    @Getter
    @Setter
    private String definition;

    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Scope scope;

    public Function() {
        this.setObjectId(new ObjectId());
        this.setImportId(this.getObjectId().toString());
    }

    public Function clone() {
        Function clone = new Function();
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        clone.setDefinition(this.definition);
        clone.setName(this.name);
        clone.setScope(this.scope);
        return clone;
    }
}
