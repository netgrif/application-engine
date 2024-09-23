package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.Init;
import com.netgrif.application.engine.importer.model.Option;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.ChoiceField;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.MapOptionsField;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import com.netgrif.application.engine.workflow.domain.DataFieldBehaviors;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class FieldBuilder<T extends Field<?>> {

    public abstract T build(Data data, Importer importer);

    public abstract DataType getType();

    public void initialize(Field<?> field) {
        field.setId(new ObjectId());
        field.setEvents(new HashMap<>());
        field.setBehaviors(new DataFieldBehaviors());
        field.setValidations(new ArrayList<>());
    }

    public String getInitExpression(Data data) {
        if (data.getInit() != null) {
            if (data.getInit().isDynamic()) {
                return data.getInit().getValue();
            }
        }
        return null;
    }

    public String resolveInit(Data data) {
//        if (data.getInits() != null && data.getInits().getInit() != null) {
//            return data.getInits().getInit().get(0).getValue();
//        }
//        if (data.getInit() != null) return data.getInit().getValue();
        return null;
    }

    public List<String> resolveInits(Data data) {
//        if (data.getInits() != null && data.getInits().getInit() != null) {
//            return data.getInits().getInit().stream().map(Init::getValue).collect(Collectors.toList());
//        }
//        if (data.getInit() != null) {
//            return List.of(data.getInit().getValue().trim().split("\\s*,\\s*"));
//        }
        return Collections.emptyList();
    }

    public void setFieldChoices(ChoiceField<?> field, Data data, Importer importer) {
//        if (data.getValues() != null && !data.getValues().isEmpty() && data.getValues().get(0).isDynamic()) {
//            field.setExpression(new Expression(data.getValues().get(0).getValue(), data.getValues().get(0).isDynamic()));
//        } else if (data.getValues() != null) {
//            List<I18nString> choices = data.getValues().stream()
//                    .map(importer::toI18NString)
//                    .collect(Collectors.toList());
//            field.getChoices().addAll(choices);
//        }
    }

    public void setFieldOptions(ChoiceField<?> field, Data data, Importer importer) {
//        if (data.getOptions() != null && data.getOptions().getInit() != null) {
//            field.setExpression(new Expression(data.getOptions().getInit().getValue(), data.getOptions().getInit().isDynamic()));
//            return;
//        }
//
//        List<I18nString> options = (data.getOptions() == null) ? new ArrayList<>() : data.getOptions().getOption().stream()
//                .map(importer::toI18NString)
//                .collect(Collectors.toList());
//        field.getChoices().addAll(options);
    }

    public void setFieldOptions(MapOptionsField<I18nString, ?> field, Data data, Importer importer) {
//        if (data.getOptions() != null && data.getOptions().getInit() != null) {
//            field.setExpression(new Expression(data.getOptions().getInit().getValue(), data.getOptions().getInit().isDynamic()));
//            return;
//        }
//
//        Map<String, I18nString> choices = (data.getOptions() == null) ? new LinkedHashMap<>() : data.getOptions().getOption().stream()
//                .collect(Collectors.toMap(Option::getKey, importer::toI18NString, (o1, o2) -> o1, LinkedHashMap::new));
//        field.setOptions(choices);
    }

    public void setDefaultValue(Field<?> field, Data data, Consumer<String> setDefault) {
        String initExpression = getInitExpression(data);
        if (initExpression != null) {
            field.setDefaultValue(Expression.ofDynamic(initExpression));
        } else {
            // TODO: release/8.0.0
            setDefault.accept(resolveInit(data));
        }
    }

    public void setDefaultValues(Field<?> field, Data data, Consumer<List<String>> setDefault) {
        String initExpression = getInitExpression(data);
        if (initExpression != null) {
            field.setDefaultValue(Expression.ofDynamic(initExpression));
        } else {
            // TODO: release/8.0.0
            setDefault.accept(resolveInits(data));
        }
    }
}
