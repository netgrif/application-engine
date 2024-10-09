package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import com.netgrif.application.engine.workflow.domain.Case;

import java.util.LinkedHashSet;
import java.util.Map;

public interface IInitValueExpressionEvaluator {

    <T> T evaluateValue(Case useCase, Field<T> defaultField, Map<String, String> params);

    Map<String, I18nString> evaluateOptions(Case useCase, MapOptionsField<I18nString, ?> field, Map<String, String> params);

    <T> LinkedHashSet<I18nString> evaluateChoices(Case useCase, ChoiceField<T> field, Map<String, String> params);

    <T> T evaluate(Case useCase, Expression<T> expression, Map<String, String> params);

    String evaluateTitle(Expression<String> expression, Map<String, String> params);
}
