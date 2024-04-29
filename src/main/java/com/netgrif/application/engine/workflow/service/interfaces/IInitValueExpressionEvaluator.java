package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.runner.Expression;
import com.netgrif.application.engine.workflow.domain.Case;

import java.util.Map;
import java.util.Set;

public interface IInitValueExpressionEvaluator {

    <T> T evaluate(Case useCase, Field<T> defaultField, Map<String, String> params);

    Map<String, I18nString> evaluateOptions(Case useCase, MapOptionsField<I18nString, ?> field, Map<String, String> params);

    Set<I18nString> evaluateChoices(Case useCase, ChoiceField<?> field, Map<String, String> params);

    I18nString evaluateCaseName(Case useCase, Expression expression, Map<String, String> params);

    Object evaluate(Case useCase, Expression expression, Map<String, String> params);
}
