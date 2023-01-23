package com.netgrif.application.engine.workflow.service.initializer;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IInitValueExpressionEvaluator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class FieldInitializer {

    protected final IInitValueExpressionEvaluator initValueExpressionEvaluator;

    public FieldInitializer(IInitValueExpressionEvaluator initValueExpressionEvaluator) {
        this.initValueExpressionEvaluator = initValueExpressionEvaluator;
    }

    public <T> Field<T> initialize(Case useCase, Field<T> original) {
//        TODO: NAE-1645
//        petriNet.getDataSet().forEach((key, field) -> {
//            if (field instanceof UserField) {
//                this.dataSet.get(key).setChoices(((UserField) field).getRoles().stream().map(I18nString::new).collect(Collectors.toSet()));
//            }
//            if (field instanceof FieldWithAllowedNets) {
//                this.dataSet.get(key).setAllowedNets(((FieldWithAllowedNets) field).getAllowedNets());
//            }
//            if (field instanceof FilterField) {
//                this.dataSet.get(key).setFilterMetadata(((FilterField) field).getFilterMetadata());
//            }
//            if (field instanceof MapOptionsField && ((MapOptionsField) field).isDynamic()) {
//                dynamicOptionsFields.add((MapOptionsField<I18nString, ?>) field);
//            }
//            if (field instanceof ChoiceField && ((ChoiceField) field).isDynamic()) {
//                dynamicChoicesFields.add((ChoiceField<?>) field);
//            }
//        });

        Field<T> field = original.clone();

        if (field.isDynamicDefaultValue()) {
            field.setRawValue(initValueExpressionEvaluator.evaluate(useCase, field));
        } else if (field.getDefaultValue() != null) {
            field.setRawValue(field.getDefaultValue());
        }

        // TODO NAE-1645: refactor?
        if (field instanceof ChoiceField) {
            ChoiceField<?> choiceField = (ChoiceField<?>) field;
            if (choiceField.isDynamic()) {
                choiceField.setChoices(initValueExpressionEvaluator.evaluateChoices(useCase, choiceField));
            }
        }

        if (field instanceof MapOptionsField) {
            MapOptionsField<I18nString,?> optionsField = (MapOptionsField<I18nString, ?>) field;
            if (optionsField.isDynamic()) {
                optionsField.setOptions(initValueExpressionEvaluator.evaluateOptions(useCase, optionsField));
            }
        }
//        populateDataSetBehavior();
        return field;
    }
}
