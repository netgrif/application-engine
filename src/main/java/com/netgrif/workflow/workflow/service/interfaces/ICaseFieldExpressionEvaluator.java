package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.workflow.workflow.domain.Case;

public interface ICaseFieldExpressionEvaluator extends IFieldExpressionEvaluator<Case> {

    I18nString evaluateCaseName(Case useCase, Expression expression);

}
