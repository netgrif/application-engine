package com.netgrif.workflow.workflow.service.interfaces;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.ChoiceField;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.runner.Expression;

import java.util.Map;
import java.util.Set;

public interface IFieldExpressionEvaluator<ENTITY> {

    <TYPE> TYPE evaluate(ENTITY entity, Field<TYPE> defaultField);

    Map<String, I18nString> evaluateOptions(ENTITY entity, MapOptionsField<I18nString, ?> field);

    Set<I18nString> evaluateChoices(ENTITY entity, ChoiceField<?> field);

    String compileValidation(ENTITY entity, Expression expression);

    Object evaluate(ENTITY entity, Expression expression);


}
