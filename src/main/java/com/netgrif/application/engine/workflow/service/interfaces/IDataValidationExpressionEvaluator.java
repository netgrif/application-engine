package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import com.netgrif.application.engine.workflow.domain.Case;

public interface IDataValidationExpressionEvaluator {

    String compile(Case useCase, Expression expression);

}