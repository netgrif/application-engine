package com.netgrif.workflow.importer.service;


import com.netgrif.workflow.importer.model.Data;
import com.netgrif.workflow.importer.model.DataType;
import com.netgrif.workflow.importer.model.Option;
import com.netgrif.workflow.importer.model.Property;
import com.netgrif.workflow.importer.service.throwable.MissingIconKeyException;
import com.netgrif.workflow.petrinet.domain.Component;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.Icon;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationField;
import com.netgrif.workflow.petrinet.domain.dataset.EnumerationMapField;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.throwable.MissingPetriNetMetaDataException;

import java.util.*;
import java.util.stream.Collectors;

@org.springframework.stereotype.Component
public class ComponentFactory {

    public Component buildComponent(com.netgrif.workflow.importer.model.Component importComponent, Importer importer, Data data) throws MissingIconKeyException{
        if (importComponent.getProperties() != null && importComponent.getProperties().getOptionIcons() != null) {
            return resolveComponent(importComponent, importer, data, null);
        }
        return buildComponent(importComponent);
    }

    public Component buildComponent(com.netgrif.workflow.importer.model.Component importComponent, Importer importer, Field field) throws MissingIconKeyException{
        if (importComponent.getProperties() != null && importComponent.getProperties().getOptionIcons() != null) {
            return resolveComponent(importComponent, importer, null, field);
        }
        return buildComponent(importComponent);
    }

    public Component buildComponent(com.netgrif.workflow.importer.model.Component importComponent){
        if (importComponent.getProperties() == null) {
            return new Component(importComponent.getName(), buildPropertyMap(importComponent.getProperty()));
        }
        return new Component(importComponent.getName(), buildPropertyMap(importComponent.getProperties().getProperty()));
    }

    public static Map<String, String> buildPropertyMap(List<Property> propertyList){
        Map<String, String> properties = new HashMap<>();
        if (propertyList != null) {
            propertyList.forEach(property -> {
                properties.put(property.getKey(), property.getValue());
            });
        }
        return properties;
    }

    public static List<Icon> buildIconsListWithValues(List<com.netgrif.workflow.importer.model.Icon> iconList, Set<I18nString> values, String fieldId) throws MissingIconKeyException{
        List<Icon> icons = new ArrayList<>();
        for (com.netgrif.workflow.importer.model.Icon icon: iconList){
            if (icon.getKey() != null && values.stream().map(I18nString::getDefaultValue).anyMatch(str -> str.equals(icon.getKey()))) {
                if (icon.getType() == null) {
                    icons.add(new Icon(icon.getKey(), icon.getValue()));
                } else {
                    icons.add(new Icon(icon.getKey(), icon.getValue(), icon.getType().value()));
                }
            } else {
                throw new MissingIconKeyException(fieldId);
            }
        }
        return icons;
    }

    public static List<Icon> buildIconsListWithOptions(List<com.netgrif.workflow.importer.model.Icon> iconList, Map<String, I18nString> options, String fieldId) throws MissingIconKeyException {
        List<Icon> icons = new ArrayList<>();
        for (com.netgrif.workflow.importer.model.Icon icon: iconList){
            if (icon.getKey() != null && options.containsKey(icon.getKey())) {
                if (icon.getType() == null) {
                    icons.add(new Icon(icon.getKey(), icon.getValue()));
                } else {
                    icons.add(new Icon(icon.getKey(), icon.getValue(), icon.getType().value()));
                }
            } else {
                throw new MissingIconKeyException(fieldId);
            }
        }
        return icons;
    }

    private Component resolveComponent(com.netgrif.workflow.importer.model.Component importComponent, Importer importer, Data data, Field field) throws MissingIconKeyException {
        if (data != null) {
            if (data.getType() == DataType.ENUMERATION){
                return new Component(importComponent.getName(), buildPropertyMap(importComponent.getProperties().getProperty()),
                        buildIconsListWithValues(importComponent.getProperties().getOptionIcons().getIcon(), data.getValues().stream().map(importer::toI18NString).collect(Collectors.toSet()), data.getId()));
            }
            return new Component(importComponent.getName(), buildPropertyMap(importComponent.getProperties().getProperty()),
                    buildIconsListWithOptions(importComponent.getProperties().getOptionIcons().getIcon(), data.getOptions().getOption().stream()
                            .collect(Collectors.toMap(Option::getKey, importer::toI18NString, (o1, o2) -> o1, LinkedHashMap::new)), data.getId()));
        }
        if (field instanceof EnumerationField){
            return new Component(importComponent.getName(), buildPropertyMap(importComponent.getProperties().getProperty()),
                    buildIconsListWithValues(importComponent.getProperties().getOptionIcons().getIcon() , ((EnumerationField) field).getChoices(), field.getImportId()));
        }
        return new Component(importComponent.getName(), buildPropertyMap(importComponent.getProperties().getProperty()),
                buildIconsListWithOptions(importComponent.getProperties().getOptionIcons().getIcon() , ((EnumerationMapField) field).getOptions(), field.getImportId()));
    }
}
