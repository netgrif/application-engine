package com.netgrif.workflow.importer.service;


import com.netgrif.workflow.importer.model.Data;
import com.netgrif.workflow.importer.model.Property;
import com.netgrif.workflow.petrinet.domain.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@org.springframework.stereotype.Component
public class ComponentFactory {

    public Component buildComponent(Data data){
        return new Component(data.getComponent().getName(), buildPropertyMap(data.getComponent().getProperty()));
    }

    public static Map<String, String> buildPropertyMap(List<Property> propertyList){
        Map<String, String> properties = new HashMap<>();
        propertyList.forEach(property -> {
            properties.put(property.getKey(), property.getValue());
        });
        return properties;
    }
}
