package com.netgrif.workflow.petrinet.domain.arcs;

import com.netgrif.workflow.petrinet.domain.Place;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.workflow.domain.DataField;
import lombok.Data;

import javax.persistence.Transient;

@Data
public class VariableArc extends Arc {

    private String fieldId;

    @Transient
    private DataField field;

    private Integer removedTokens;

    public VariableArc() {
    }

    public VariableArc(int fieldImportId) {
        super();
        this.multiplicity = fieldImportId;
    }

    @Override
    public boolean isExecutable() {
        if (source instanceof Transition)
            return true;
        if (field == null || field.getValue() == null)
            throw new IllegalStateException("Field "+ fieldId + " has null value");
        double multiplicity = Double.parseDouble(field.getValue().toString());
        return ((Place) source).getTokens() >= multiplicity;
    }

    @Override
    public void execute() {
        double multiplicity = Double.parseDouble(field.getValue().toString());
        if (source instanceof  Place) {
            removedTokens = (int) multiplicity;
            getPlace().removeTokens(removedTokens);
        } else {
            getPlace().addTokens((int) multiplicity);
        }
    }

    @Override
    public void rollbackExecution() {
        ((Place) source).addTokens(removedTokens);
    }

    @Override
    public void setMultiplicity(Integer multiplicity) {
        // Readonly
    }

    @SuppressWarnings("Duplicates")
    @Override
    public VariableArc clone() {
        VariableArc clone = new VariableArc();
        clone.setSourceId(this.sourceId);
        clone.setDestinationId(this.destinationId);
        clone.setMultiplicity(this.multiplicity);
        clone.setObjectId(this.getObjectId());
        clone.setImportId(this.importId);
        clone.setField(this.field);
        clone.setFieldId(this.fieldId);
        return clone;
    }
}