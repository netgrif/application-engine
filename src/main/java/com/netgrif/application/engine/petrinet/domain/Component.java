package com.netgrif.application.engine.petrinet.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
@AllArgsConstructor
public class Component implements Serializable {

    private static final long serialVersionUID = 5852012111111766631L;

    private String id;
    private Map<String, String> properties;

    public Component() {
        this.properties = new HashMap<>();
    }

    public Component(String id) {
        this();
        this.id = id;
    }

    @Override
    public Component clone() {
        return new Component(this.id, new HashMap<>(this.properties));
    }
}
