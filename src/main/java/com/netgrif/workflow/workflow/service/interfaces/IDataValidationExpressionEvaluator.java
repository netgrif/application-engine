package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.workflow.workflow.domain.Case;

public interface IDataValidationExpressionEvaluator {

    String compile(Case useCase, Expression expression);

}