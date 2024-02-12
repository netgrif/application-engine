package com.netgrif.application.engine.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

public class Icon {
    @Getter
    @Setter
    private String key;

    @Getter
    @Setter
    private String value;

    @Getter
    @Setter
    private String type;

    public Icon() {
    }

    public Icon(String key, String value) {
        this.key = key;
        this.value = value;
        this.type = "material";
    }

    public Icon(String key, String value, String type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    @Override
    public Icon clone() {
        Icon clone = new Icon();
        clone.setKey(this.key);
        clone.setValue(this.value);
        clone.setType(this.type);
        return clone;
    }
}
