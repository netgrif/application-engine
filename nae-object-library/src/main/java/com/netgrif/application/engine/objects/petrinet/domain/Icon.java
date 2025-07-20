package com.netgrif.application.engine.objects.petrinet.domain;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class Icon implements Serializable {

    @Serial
    private static final long serialVersionUID = -140211037056216078L;

    private String key;

    private String value;

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
