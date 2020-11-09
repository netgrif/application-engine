package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

public class Component {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Map<String, String> properties;

    public Component(){}

    public Component(String name){
        this.name = name;
        this.properties = new HashMap<>();
    }

    public Component(String name, Map<String, String> properties){
        this(name);
        this.properties = properties;
    }
}
