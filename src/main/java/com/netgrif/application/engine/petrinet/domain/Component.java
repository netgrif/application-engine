package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.utils.UniqueKeyMapWrapper;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Component implements Serializable {

    private static final long serialVersionUID = 5852012111111766631L;

    private String id;
    private UniqueKeyMapWrapper<String> properties;

    public Component() {
        this.properties = new UniqueKeyMapWrapper<>();
    }

    public Component(String id) {
        this();
        this.id = id;
    }

    @Override
    public Component clone() {
        return new Component(this.id, new UniqueKeyMapWrapper<>(this.properties));
    }
}
