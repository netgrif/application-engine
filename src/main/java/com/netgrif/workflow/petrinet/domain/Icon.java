package com.netgrif.workflow.petrinet.domain;

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
}
