package com.netgrif.application.engine.petrinet.service;

import com.netgrif.application.engine.workflow.domain.arcs.Multiplicity;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ExpressionRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MultiplicityEvaluator {

    @Autowired
    private ExpressionRunner runner;

    public int evaluate(Multiplicity multiplicity, Case useCase) {
        Object run = runner.run(multiplicity, useCase);
        if (!(run instanceof Integer)) {
            throw new IllegalStateException("Multiplicity could not be evaluated");
        }
        return (int) run;
    }
}
