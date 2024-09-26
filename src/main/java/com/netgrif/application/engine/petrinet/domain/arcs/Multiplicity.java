package com.netgrif.application.engine.petrinet.domain.arcs;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Multiplicity extends Expression<Integer> {
    private Integer multiplicity;
    private ReferenceType referenceType;

    public Multiplicity(int multiplicity) {
        super(multiplicity, null);
        this.multiplicity = multiplicity;
    }

    public Multiplicity(String definition) {
        super(null, definition);
    }

    public Multiplicity(String referenceId, ReferenceType type) {
        this(referenceId);
        this.referenceType = type;
    }

    public Multiplicity clone() {
        if (this.getDefaultValue() != null) {
            return new Multiplicity(this.multiplicity);
        }
        return new Multiplicity(this.getDefinition());
    }
}
