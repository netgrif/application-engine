package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ExpressionRunner;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataValidationExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DataValidationExpressionEvaluator implements IDataValidationExpressionEvaluator {

    @Autowired
    protected ExpressionRunner runner;

    @Override
    public String compile(Case useCase, Expression expression) {
        return runner.run(useCase, expression).toString();
    }

}
