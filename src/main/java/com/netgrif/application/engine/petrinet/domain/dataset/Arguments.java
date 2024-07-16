package com.netgrif.application.engine.petrinet.domain.dataset;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Arguments implements Serializable {

    private static final long serialVersionUID = -2385696520525471923L;

    protected ArgumentsType type;
    protected List<String> argument;

    @Override
    public Arguments clone() {
        Arguments cloned =  new Arguments();
        cloned.setType(type);
        cloned.setArgument(argument);
        return cloned;
    }
}
