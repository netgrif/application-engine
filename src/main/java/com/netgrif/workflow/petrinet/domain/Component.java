package com.netgrif.workflow.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Component {
    @Getter
    @Setter
    private String name;

    @Getter
    @Setter
    private Map<String, String> properties;

    @Getter
    @Setter
    private List<Icon> optionIcons;

    public Component() {
    }

    public Component(String name) {
        this.name = name;
        this.properties = new HashMap<>();
        this.optionIcons = new ArrayList<>();
    }

    public Component(String name, Map<String, String> properties) {
        this(name);
        this.properties = properties;
    }

    public Component(String name, Map<String, String> properties, List<Icon> optionIcons) {
        this(name);
        this.properties = properties;
        this.optionIcons = optionIcons;
    }
}
