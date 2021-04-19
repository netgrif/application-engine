package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.workflow.domain.Case;

public interface IInitValueExpressionEvaluator {

    <T> T evaluate(Case useCase, Field<T> defaultField);
}
