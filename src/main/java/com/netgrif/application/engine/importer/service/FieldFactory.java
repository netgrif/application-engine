package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.configuration.properties.DatabaseProperties;
import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.model.Valid;
import com.netgrif.application.engine.importer.service.builder.FieldBuilder;
import com.netgrif.application.engine.importer.service.throwable.MissingIconKeyException;
import com.netgrif.application.engine.importer.service.validation.IDataValidator;
import com.netgrif.application.engine.petrinet.domain.Component;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.Validation;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
@Slf4j
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

        if (data.getValidations() != null) {
            List<com.netgrif.application.engine.importer.model.Validation> list = data.getValidations().getValidation();
            for (com.netgrif.application.engine.importer.model.Validation item : list) {
                field.addValidation(new Validation(item.getName(), item.getArguments(), importer.toI18NString(item.getMessage())));
            }
        }
        if (data.getComponent() != null) {
            Component component = componentFactory.buildComponent(data.getComponent(), importer, data);
            field.setComponent(component);
        }
        if (data.getView() != null) {
            log.warn("Data attribute [view] in field [{}] is deprecated.", field.getImportId());
        }
        if (data.getFormat() != null) {
            log.warn("Data attribute [format] in field [{}] is deprecated.", field.getImportId());
        }

        setEncryption(field, data);

        dataValidator.checkDeprecatedAttributes(data);
        return field;
    }

//TODO: release/8.0.0 merge check
    /*private void resolveComponent(Field field, Case useCase, String transitionId) {
        if (useCase.getDataField(field.getStringId()).hasComponent(transitionId)) {
            field.setComponent(useCase.getDataField(field.getStringId()).getDataRefComponents().get(transitionId));
        } else if (useCase.getDataField(field.getStringId()).hasComponent()) {
            field.setComponent(useCase.getDataField(field.getStringId()).getComponent());
        }
    }*/
    /* private StringCollectionField buildStringCollectionField(Data data, Importer importer) {
        StringCollectionField field = new StringCollectionField();
        setDefaultValues(field, data, defaultValues -> {
            if (defaultValues != null) {
                field.setDefaultValue(defaultValues);
            }
        });
        return field;
    }*/

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