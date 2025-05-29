package com.netgrif.application.engine.petrinet.domain.dataset.logic.action;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Action implements Serializable {

    private static final long serialVersionUID = 3687481049847555522L;

    private String importId;
    private ObjectId id = new ObjectId();
    private String definition;
    // TODO: release/8.0.0 replace with set action type
    private SetDataType setDataType = SetDataType.VALUE;

    @Override
    public String toString() {
        return definition;
    }

    @Override
    public Action clone() {
        Action clone = new Action();
        clone.setId(new ObjectId(this.getId().toString()));
        clone.setDefinition(this.definition);
        clone.setImportId(this.importId);
        clone.setSetDataType(this.setDataType);
        return clone;
    }
}