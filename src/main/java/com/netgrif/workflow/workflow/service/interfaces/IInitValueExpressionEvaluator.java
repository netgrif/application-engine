package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.ChoiceField;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.workflow.workflow.domain.Case;

import java.util.Map;
import java.util.Set;

public interface IInitValueExpressionEvaluator {

    <T> T evaluate(Case useCase, Field<T> defaultField);

    Map<String, I18nString> evaluateOptions(Case useCase, MapOptionsField<I18nString, ?> field);

    Set<I18nString> evaluateChoices(Case useCase, ChoiceField<?> field);

    Object evaluate(Case useCase, String expression);
}
