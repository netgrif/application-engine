package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Arguments implements Serializable {

    private static final long serialVersionUID = -2385696520525471923L;

    private ArrayList<Expression<String>> argument;

    public Arguments() {
        this.argument = new ArrayList<>();
    }

    public void addArgument(Expression<String> argument) {
        this.argument.add(argument);
    }

    @Override
    public Arguments clone() {
        Arguments cloned =  new Arguments();
        if (argument != null) {
            ArrayList<Expression<String>> clonedArgument = new ArrayList<>();
            argument.forEach(a -> clonedArgument.add(a.clone()));
            cloned.setArgument(clonedArgument);
        }
        return cloned;
    }
}
