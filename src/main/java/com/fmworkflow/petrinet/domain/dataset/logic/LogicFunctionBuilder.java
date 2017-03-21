package com.fmworkflow.petrinet.domain.dataset.logic;

import java.util.List;

public class LogicFunctionBuilder {
    public IDataFunction build(List<String> names) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        IDataFunction logic = null;
        for (String name : names) {
            IDataFunction newFunction = (IDataFunction) Class.forName(name).newInstance();
            if (logic == null)
                logic = newFunction;
            else
                logic.compose(newFunction);
        }
        return logic;
    }
}
