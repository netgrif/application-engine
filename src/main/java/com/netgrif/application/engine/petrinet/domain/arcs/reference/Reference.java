package com.netgrif.application.engine.petrinet.domain.arcs.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;

import java.io.Serializable;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reference implements Serializable {

    private static final long serialVersionUID = 7082645066561158952L;

    private String reference;

    private Type type;

    @Transient
    private Referencable referencable;

    public Reference(String reference, Type type) {
        this.reference = reference;
        this.type = type;
    }

    public int getMultiplicity() {
        int multiplicity = this.referencable.getMultiplicity();
        if (multiplicity < 0) {
            throw new IllegalStateException("Referenced object " + reference + " has invalid multiplicity: " + multiplicity);
        }
        return multiplicity;
    }

    public Reference clone() {
        return new Reference(this.reference, this.type);
    }

}