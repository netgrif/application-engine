package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.configuration.properties.DatabaseProperties;
import com.netgrif.application.engine.importer.model.Argument;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.builder.FieldBuilder;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.Validation;
import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
@Slf4j
public final class FieldFactory {

    private final DatabaseProperties properties;
    private final Map<DataType, FieldBuilder<?>> builders;

    public FieldFactory(DatabaseProperties properties, List<FieldBuilder<?>> builders) {
        this.properties = properties;
        this.builders = builders.stream().collect(Collectors.toMap(FieldBuilder::getType, Function.identity()));
    }

    Field<?> getField(Data data, Importer importer) throws IllegalArgumentException, MissingIconKeyException {
        FieldBuilder<?> builder = builders.get(data.getType());
        if (builder == null) {
            throw new IllegalArgumentException("Field " + data.getId() + " has unsupported type " + data.getType());
        }
        Field<?> field = builder.build(data, importer);
        field.setImportId(data.getId());
        field.setTitle(importer.toI18NString(data.getTitle()));
        if (data.isImmediate() != null) {
            field.setImmediate(data.isImmediate());
        }
        if (data.getDesc() != null) {
            field.setDescription(importer.toI18NString(data.getDesc()));
        }
        if (data.getPlaceholder() != null) {
            field.setPlaceholder(importer.toI18NString(data.getPlaceholder()));
        }
        if (data.getValidations() != null) {
            List<com.netgrif.application.engine.importer.model.Validation> list = data.getValidations().getValidation();
            for (com.netgrif.application.engine.importer.model.Validation item : list) {
                Arguments clientArguments = null;
                if (item.getClientArguments() != null) {
                    clientArguments = new Arguments(item.getClientArguments().getArgument().stream().map(arg -> new Argument(arg.getValue(), arg.isDynamic())).collect(Collectors.toList()));
                }
                Arguments serverArguments = null;
                if (item.getServerArguments() != null) {
                    serverArguments = new Arguments(item.getServerArguments().getArgument().stream().map(arg -> new Argument(arg.getValue(), arg.isDynamic())).collect(Collectors.toList()));
                }
                field.addValidation(new Validation(item.getName(), clientArguments, serverArguments, importer.toI18NString(item.getMessage())));
            }
        }
        if (data.getComponent() != null) {
            Component component = importer.createComponent(data.getComponent());
            field.setComponent(component);
        }
//
        setEncryption(field, data);
//        dataValidator.checkDeprecatedAttributes(data);
        return field;
    }

    private Validation createValidation(com.netgrif.application.engine.importer.model.Validation item, Importer importer) {
        Validation validation = new Validation();
        validation.setName(item.getName());
        validation.setMessage(importer.toI18NString(item.getMessage()));
        if (item.getClientArguments() != null) {
            for (Argument argument : item.getClientArguments().getArgument()) {
                validation.getClientArguments().add(createArgument(argument.getValue(), argument.isDynamic()));
            }
        }
        if (item.getServerArguments() != null) {
            for (Argument argument : item.getServerArguments().getArgument()) {
                validation.getServerArguments().add(createArgument(argument.getValue(), argument.isDynamic()));
            }
        }
        return validation;
    }

    private Expression<String> createArgument(String value, Boolean dynamic) {
        if (dynamic != null && dynamic) {
            return Expression.ofDynamic(value);
        }
        return Expression.ofStatic(value);
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