package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.configuration.properties.DatabaseProperties;
import com.netgrif.application.engine.importer.model.Argument;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.Valid;
import com.netgrif.application.engine.importer.service.builder.FieldBuilder;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.importer.service.validation.IDataValidator;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.ValidationRule;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@org.springframework.stereotype.Component
public final class FieldFactory {

    private final DatabaseProperties properties;
    private final ComponentFactory componentFactory;
    private final IDataValidator dataValidator;
    private final Map<DataType, FieldBuilder<?>> builders;

    public FieldFactory(DatabaseProperties properties, List<FieldBuilder<?>> builders, ComponentFactory componentFactory, IDataValidator dataValidator) {
        this.properties = properties;
        this.builders = builders.stream().collect(Collectors.toMap(FieldBuilder::getType, Function.identity()));
        this.componentFactory = componentFactory;
        this.dataValidator = dataValidator;
    }

    Field<?> getField(Data data, Importer importer) throws IllegalArgumentException, MissingIconKeyException {
        FieldBuilder<?> builder = builders.get(data.getType());
        if (builder == null) {
            throw new IllegalArgumentException("Field " + data.getId() + " has unsupported type " + data.getType());
        }
        Field<?> field = builder.build(data, importer);
        field.setName(importer.toI18NString(data.getTitle()));
        field.setImportId(data.getId());
        if (data.isImmediate() != null) {
            field.setImmediate(data.isImmediate());
        }
        if (data.getLength() != null) {
            field.setLength(data.getLength());
        }
        if (data.getDesc() != null)
            field.setDescription(importer.toI18NString(data.getDesc()));

        if (data.getPlaceholder() != null)
            field.setPlaceholder(importer.toI18NString(data.getPlaceholder()));


        if (data.getValid() != null) {
            List<Valid> list = data.getValid();
            for (Valid item : list) {
                System.out.println(item);
//                field.addValidation(item);
//                field.addValidation(makeValidation(item.getValue(), null, item.isDynamic())); //TODO: JOZIII
            }
        }

        if (data.getValidations() != null) {
            List<com.netgrif.application.engine.importer.model.Validation> list = data.getValidations().getValidation();
            for (com.netgrif.application.engine.importer.model.Validation item : list) {
                if (item != null
                        && item.getName() != null
                        && item.getArguments() != null) {
                    field.addValidation(makeValidation(item.getName().getValue(), item.getArguments().getArgument(), importer.toI18NString(item.getMessage().get(0)))); //TODO: JOZIII
                }
            }
        }

        if (data.getComponent() != null) {
            Component component = componentFactory.buildComponent(data.getComponent(), importer, data);
            field.setComponent(component);
        }
        if (data.getView() != null) {
            log.warn("Data attribute [view] in field [" + field.getImportId() + "] is deprecated.");
        }
        if (data.getFormat() != null) {
            log.warn("Data attribute [format] in field [" + field.getImportId() + "] is deprecated.");
        }

        setEncryption(field, data);

        dataValidator.checkDeprecatedAttributes(data);
        return field;
    }

    private com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation makeValidation(String name, List rule, I18nString message) {
//        return dynamic ?
//                new DynamicValidation(rule, message) :
        return new com.netgrif.application.engine.petrinet.domain.dataset.logic.validation.Validation(name, makeValidationRules(rule), message);
    }


    private Map<String, ValidationRule> makeValidationRules(List rule) {
        return (Map<String, ValidationRule>) rule.stream()
                .filter(Objects::nonNull)
                .map(this::convertToValidationRule)
                .collect(Collectors.toMap(ValidationRule::getName, Function.identity()));
    }

    private ValidationRule convertToValidationRule(Object object) {
        if (object instanceof Argument) {
            Argument argument = (Argument) object;
            ValidationRule validationRule = new ValidationRule();
            validationRule.setName(argument.getKey());
            validationRule.setRule(argument.getValue());
            validationRule.setDynamic(Boolean.TRUE.equals(argument.isDynamic()));
            return validationRule;
        } else {
            return null;
        }
    }

    private void setEncryption(Field<?> field, Data data) {
        if (data.getEncryption() != null && data.getEncryption().isValue()) {
            String encryption = data.getEncryption().getAlgorithm();
            if (encryption == null) {
                encryption = properties.getAlgorithm();
            }
            field.setEncryption(encryption);
        }
    }
}