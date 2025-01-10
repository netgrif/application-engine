package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.configuration.properties.DatabaseProperties;
import com.netgrif.application.engine.importer.model.Argument;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.builder.FieldBuilder;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.workflow.domain.Component;
import com.netgrif.application.engine.workflow.domain.dataset.Arguments;
import com.netgrif.application.engine.workflow.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.dataset.Validation;
import com.netgrif.application.engine.workflow.domain.dataset.logic.Expression;
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
            createValidation(data, importer, field);
        }
        if (data.getComponent() != null) {
            Component component = importer.createComponent(data.getComponent());
            field.setComponent(component);
        }
        field.setScope(data.getScope());
        setEncryption(field, data);
//        dataValidator.checkDeprecatedAttributes(data);
        return field;
    }

    private void createValidation(Data data, Importer importer, Field<?> field) {
        for (com.netgrif.application.engine.importer.model.Validation item : data.getValidations().getValidation()) {
            Arguments clientArguments = new Arguments();
            if (item.getClientArguments() != null) {
                this.createArguments(item.getServerArguments().getArgument(), clientArguments);
            }
            Arguments serverArguments = new Arguments();
            if (item.getServerArguments() != null) {
                this.createArguments(item.getServerArguments().getArgument(), serverArguments);
            }
            field.addValidation(new Validation(item.getName(), clientArguments, serverArguments, importer.toI18NString(item.getMessage())));
        }
    }

    private void createArguments(List<Argument> importedArguments, Arguments arguments) {
        for (Argument importedArgument : importedArguments) {
            Expression<String> argument;
            if (importedArgument.isDynamic() != null && importedArgument.isDynamic()) {
                argument = Expression.ofDynamic(importedArgument.getValue());
            } else {
                argument = Expression.ofStatic(importedArgument.getValue());
            }
            arguments.addArgument(argument);
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