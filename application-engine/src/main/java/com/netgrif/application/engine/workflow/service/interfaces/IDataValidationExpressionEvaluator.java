package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.application.engine.objects.workflow.domain.Case;

public interface IDataValidationExpressionEvaluator {

    String compile(Case useCase, Expression expression);

}
