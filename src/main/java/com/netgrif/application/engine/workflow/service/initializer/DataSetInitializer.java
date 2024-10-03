package com.netgrif.application.engine.workflow.service.initializer;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.petrinet.domain.dataset.TaskField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IInitValueExpressionEvaluator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Map;

@Component
public class DataSetInitializer {

    protected final IInitValueExpressionEvaluator initValueExpressionEvaluator;

    public DataSetInitializer(IInitValueExpressionEvaluator initValueExpressionEvaluator) {
        this.initValueExpressionEvaluator = initValueExpressionEvaluator;
    }

    public void populateDataSet(Case useCase, Map<String, String> params) {
        useCase.getProcess().getDataSet().forEach((fieldId, field) -> {
            Field<?> useCaseField = field.clone();
            useCase.getDataSet().put(fieldId, useCaseField);
            if (field.isImmediate()) {
                useCase.getImmediateDataFields().add(field.getStringId());
                useCase.getImmediateData().add(useCaseField);
            }
            if (useCaseField instanceof TaskField) {
                return;
            }
            if (useCaseField instanceof ChoiceField) {
                ChoiceField<?> choiceField = (ChoiceField<?>) useCaseField;
                if (choiceField.isDynamic()) {
                    initializeChoices(useCase, choiceField, params);
                }
            }
            if (useCaseField instanceof MapOptionsField) {
                MapOptionsField<I18nString, ?> optionsField = (MapOptionsField<I18nString, ?>) useCaseField;
                if (optionsField.isDynamic()) {
                    initializeOptions(useCase, optionsField, params);
                }
            }
            if (useCaseField.getDefaultValue() != null) {
                if (useCaseField.getDefaultValue().isDynamic()) {
                    initializeValue(useCase, useCaseField, params);
                } else {
                    useCaseField.applyDefaultValue();
                }
            }
        });
    }

    public <T> void initializeValue(Case useCase, Field<T> field, Map<String, String> params) {
        field.setRawValue(initValueExpressionEvaluator.evaluate(useCase, field, params));
    }

    public void initializeChoices(Case useCase, ChoiceField<?> field, Map<String, String> params) {
        field.setChoices(initValueExpressionEvaluator.evaluateChoices(useCase, field, params));
    }

    public void initializeOptions(Case useCase, MapOptionsField<I18nString, ?> field, Map<String, String> params) {
        field.setOptions(initValueExpressionEvaluator.evaluateOptions(useCase, field, params));
    }
}
