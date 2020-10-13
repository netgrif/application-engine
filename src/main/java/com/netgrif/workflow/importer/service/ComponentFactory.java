package com.netgrif.workflow.importer.service;


import com.netgrif.workflow.importer.model.Data;
import com.netgrif.workflow.petrinet.domain.Component;

@org.springframework.stereotype.Component
public class ComponentFactory {

    public Component buildComponent(Data data){
                return new Component(data.getComponent().getName());
    }
}
