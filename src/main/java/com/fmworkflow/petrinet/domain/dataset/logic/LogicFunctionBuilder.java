package com.fmworkflow.petrinet.domain.dataset.logic;

import java.util.List;

public class LogicFunctionBuilder {
    public LogicFunction build(List<String> names) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        LogicFunction logic = null;
        for (String name : names) {
            LogicFunction newFunction = (LogicFunction) Class.forName(name).newInstance();
            if (logic == null)
                logic = newFunction;
            else
                logic.compose(newFunction);
        }
        return logic;
    }
}
