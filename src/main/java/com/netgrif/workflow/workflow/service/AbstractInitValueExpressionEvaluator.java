package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.dataset.ChoiceField;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.workflow.workflow.service.interfaces.IInitValueExpressionEvaluator;

import java.util.*;
import java.util.stream.Collectors;

public abstract class AbstractInitValueExpressionEvaluator<ENTITY> implements IInitValueExpressionEvaluator<ENTITY> {

    @Override
    public <T> T evaluate(ENTITY entity, Field<T> defaultField) {
        return (T) evaluate(entity, defaultField.getInitExpression());
    }

    @Override
    public Map<String, I18nString> evaluateOptions(ENTITY entity, MapOptionsField<I18nString, ?> field) {
        Object result = evaluate(entity, field.getExpression());
        if (!(result instanceof Map)) {
            throw new IllegalArgumentException("[" + getStringId(entity) + "] Dynamic options not an instance of Map: " + field.getImportId());
        }
        Map<String, Object> map = (Map) result;
        if (map.values().stream().anyMatch(it -> !(it instanceof I18nString))) {
            return map.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, it -> new I18nString(it.getValue().toString()), (o1, o2) -> o1, LinkedHashMap::new));
        } else {
            return (Map<String, I18nString>) result;
        }
    }

    @Override
    public Set<I18nString> evaluateChoices(ENTITY entity, ChoiceField<?> field) {
        Object result = evaluate(entity, field.getExpression());
        if (!(result instanceof Collection)) {
            throw new IllegalArgumentException("[" + getStringId(entity) + "] Dynamic choices not an instance of Collection: " + field.getImportId());
        }
        Collection<Object> collection = (Collection) result;
        return collection.stream().map(it -> (it instanceof I18nString) ? (I18nString) it : new I18nString(it.toString())).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    abstract String getStringId(ENTITY entity);
}
