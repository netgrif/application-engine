package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.workflow.domain.I18nString;
import com.netgrif.application.engine.workflow.domain.dataset.ChoiceField;
import com.netgrif.application.engine.workflow.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.workflow.domain.dataset.logic.Expression;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ExpressionRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IInitValueExpressionEvaluator;
import groovy.lang.GString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpressionEvaluator implements IInitValueExpressionEvaluator {

    @Autowired
    private ExpressionRunner runner;

    @Override
    public <T> T evaluateValue(Case useCase, Field<T> defaultField, Map<String, String> params) {
        return (T) runner.run(defaultField.getDefaultValue(), useCase, defaultField, params);
    }

    @Override
    public LinkedHashMap<String, I18nString> evaluateOptions(Case useCase, MapOptionsField<I18nString, ?> field, Map<String, String> params) {
        Object result = evaluate(useCase, field.getOptionsExpression(), params);
        if (!(result instanceof Map)) {
            throw new IllegalArgumentException("[" + useCase.getStringId() + "] Dynamic options not an instance of Map: " + field.getImportId());
        }
        Map<String, Object> map = (Map) result;
        if (map.values().stream().anyMatch(it -> !(it instanceof I18nString))) {
            return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, it -> new I18nString(it.getValue().toString()), (o1, o2) -> o1, LinkedHashMap::new));
        } else {
            return (LinkedHashMap<String, I18nString>) result;
        }
    }

    @Override
    public <T> LinkedHashSet<I18nString> evaluateChoices(Case useCase, ChoiceField<T> field, Map<String, String> params) {
        Object result = evaluate(useCase, field.getExpression(), params);
        if (result == null) {
            throw new IllegalArgumentException("[" + useCase.getStringId() + "] Dynamic choices not an instance of Collection: " + field.getImportId());
        }
        Collection<Object> collection = (Collection) result;
        return collection.stream().map(it -> (it instanceof I18nString) ? (I18nString) it : new I18nString(it.toString())).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public <T> T evaluate(Case useCase, Expression<T> expression, Map<String, String> params) {
        return runner.run(expression, useCase, null, params);
    }

    @Override
    public String evaluateTitle(Expression<String> expression, Map<String, String> params) {
        Object title = this.evaluate(null, expression, params);
        if (title instanceof GString) {
            // due to ClassCastException
            return title.toString();
        }
        return (String) title;
    }
}
