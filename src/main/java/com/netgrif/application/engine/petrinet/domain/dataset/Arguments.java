package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Arguments implements Serializable {

    private static final long serialVersionUID = -2385696520525471923L;

    protected List<Argument> argument;

    @Override
    public Arguments clone() {
        Arguments cloned =  new Arguments();
        if (argument != null) {
            List<Argument> clonedArgument = new ArrayList<>();
            argument.forEach(a -> clonedArgument.add(a.clone()));
            cloned.setArgument(clonedArgument);
        }
        return cloned;
    }
}
