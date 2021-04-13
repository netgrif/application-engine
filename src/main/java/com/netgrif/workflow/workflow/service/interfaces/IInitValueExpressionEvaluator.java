package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.dataset.FieldWithDefault;
import com.netgrif.workflow.workflow.domain.Case;

public interface IInitValueExpressionEvaluator {

    <T> T evaluate(Case useCase, FieldWithDefault<T> defaultField);
}
