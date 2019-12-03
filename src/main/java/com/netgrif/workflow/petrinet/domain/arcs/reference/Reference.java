package com.netgrif.workflow.petrinet.domain.arcs.reference;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Transient;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Reference {

    private String reference;

    private Type type;

    @Transient
    private Referencable referencable;

    public Reference(String reference, Type type) {
        this.reference = reference;
        this.type = type;
    }
}