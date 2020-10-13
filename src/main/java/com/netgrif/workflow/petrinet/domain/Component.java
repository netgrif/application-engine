package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

public class Component {
    @Getter
    @Setter
    private String name;

    public Component(){}

    public Component(String name){
        this.name = name;
    }
}
