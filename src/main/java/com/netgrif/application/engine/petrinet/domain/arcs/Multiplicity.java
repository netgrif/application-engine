package com.netgrif.application.engine.petrinet.domain.arcs;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Multiplicity extends Expression {
    private Integer multiplicity;

    public Multiplicity(int multiplicity) {
        super("", false);
        this.multiplicity = multiplicity;
    }

    public Multiplicity(String definition, boolean dynamic) {
        super(definition, dynamic);
    }

    private Multiplicity(Integer multiplicity, String definition, boolean dynamic) {
        super(definition, dynamic);
        this.multiplicity = multiplicity;
    }

    public Multiplicity clone() {
        return new Multiplicity(this.multiplicity, this.getDefinition(), this.isDynamic());
    }
}
