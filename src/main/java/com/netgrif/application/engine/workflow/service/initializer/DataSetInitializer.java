package com.netgrif.application.engine.workflow.service.initializer;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IInitValueExpressionEvaluator;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
public class DataSetInitializer {

    protected final IInitValueExpressionEvaluator initValueExpressionEvaluator;

    public DataSetInitializer(IInitValueExpressionEvaluator initValueExpressionEvaluator) {
        this.initValueExpressionEvaluator = initValueExpressionEvaluator;
    }

    public void populateDataSet(Case useCase) {
        ArrayList<Field<?>> dynamicValueFields = new ArrayList<>();
        ArrayList<ChoiceField<?>> dynamicChoiceFields = new ArrayList<>();
        ArrayList<MapOptionsField<I18nString, ?>> dynamicOptionFields = new ArrayList<>();
        useCase.getPetriNet().getDataSet().forEach((fieldId, field) -> {
            Field<?> useCaseField = field.clone();
            useCase.getDataSet().put(fieldId, useCaseField);
            if (field.isImmediate()) {
                useCase.getImmediateDataFields().add(field.getStringId());
                useCase.getImmediateData().add(useCaseField);
            }
            if (useCaseField.isDynamicDefaultValue()) {
                dynamicValueFields.add(useCaseField);
            } else if (useCaseField.getDefaultValue() != null) {
                useCaseField.applyDefaultValue();
            }
            if (useCaseField instanceof ChoiceField) {
                ChoiceField<?> choiceField = (ChoiceField<?>) useCaseField;
                if (choiceField.isDynamic()) {
                    dynamicChoiceFields.add(choiceField);
                }
            }
            if (useCaseField instanceof MapOptionsField) {
                MapOptionsField<I18nString,?> optionsField = (MapOptionsField<I18nString, ?>) useCaseField;
                if (optionsField.isDynamic()) {
                    dynamicOptionFields.add(optionsField);
                }
            }
        });

        dynamicChoiceFields.forEach(f -> this.initializeChoices(useCase, f));
        dynamicOptionFields.forEach(f -> this.initializeOptions(useCase, f));
        dynamicValueFields.forEach(f -> this.initializeValue(useCase, f));
    }

    public <t> void initializeValue(Case useCase, Field<t> field) {
        field.setRawValue(initValueExpressionEvaluator.evaluate(useCase, field));
    }

    public void initializeChoices(Case useCase, ChoiceField<?> field) {
        field.setChoices(initValueExpressionEvaluator.evaluateChoices(useCase, field));
    }

    public void initializeOptions(Case useCase, MapOptionsField<I18nString, ?> field) {
        field.setOptions(initValueExpressionEvaluator.evaluateOptions(useCase, field));
    }
}
