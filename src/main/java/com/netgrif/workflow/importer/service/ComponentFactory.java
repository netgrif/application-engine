package com.netgrif.workflow.importer.service;


import com.netgrif.workflow.importer.model.Data;
import com.netgrif.workflow.importer.model.Property;
import com.netgrif.workflow.petrinet.domain.Component;
import com.netgrif.workflow.petrinet.domain.Icon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
public class ComponentFactory {

    public Component buildComponent(com.netgrif.workflow.importer.model.Component importComponent){
        if (importComponent.getProperties() == null) {
            return new Component(importComponent.getName(), buildPropertyMap(importComponent.getProperty()));
        } else {
            if (importComponent.getProperties().getOptionIcons() == null) {
                return new Component(importComponent.getName(), buildPropertyMap(importComponent.getProperties().getProperty()));
            }
            return new Component(importComponent.getName(), buildIconsList(importComponent.getProperties().getOptionIcons().getIcon()));
        }
    }

    public static Map<String, String> buildPropertyMap(List<Property> propertyList){
        Map<String, String> properties = new HashMap<>();
        propertyList.forEach(property -> {
            properties.put(property.getKey(), property.getValue());
        });
        return properties;
    }

    public static List<Icon> buildIconsList(List<com.netgrif.workflow.importer.model.Icon> iconList){
        List<Icon> icons = new ArrayList<>();
        iconList.forEach(icon -> {
            if (icon.getType() == null) {
                icons.add(new Icon(icon.getKey(), icon.getValue()));
            } else {
                icons.add(new Icon(icon.getKey(), icon.getValue(), icon.getType().value()));
            }
        });
        return icons;
    }
}
