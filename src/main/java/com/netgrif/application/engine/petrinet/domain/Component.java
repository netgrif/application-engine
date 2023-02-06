package com.netgrif.application.engine.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    @Override
    public Component clone() {
        return new Component(this.name, new HashMap<>(this.properties), this.optionIcons == null ? new ArrayList<>() : this.optionIcons.stream().map(Icon::clone).collect(Collectors.toList()));
    }
}
