package com.fmworkflow.petrinet.domain.dataset.logic;

import java.util.List;

public class LogicFunctionBuilder {
    public ILogicFunction build(List<String> names) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        ILogicFunction logic = null;
        for (String name : names) {
            ILogicFunction newFunction = (ILogicFunction) Class.forName(name).newInstance();
            if (logic == null)
                logic = newFunction;
            else
                logic.compose(newFunction);
        }
        return logic;
    }
}
