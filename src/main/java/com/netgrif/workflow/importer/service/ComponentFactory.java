package com.netgrif.workflow.importer.service;


import com.netgrif.workflow.importer.model.Component;
import com.netgrif.workflow.importer.model.Data;

@org.springframework.stereotype.Component
public class ComponentFactory {

    public Component buildComponent(Data data){
                return data.getComponent();
    }
}
