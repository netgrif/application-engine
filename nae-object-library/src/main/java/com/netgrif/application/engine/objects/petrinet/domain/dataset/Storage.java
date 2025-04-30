package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

@Setter
@Getter
public class Storage implements Serializable {

    @Getter
    @Serial
    private static final long serialVersionUID = 9172755427378929926L;
    private String type;
    private String host;

    public Storage() {
        this.type = "local";
    }

    public Storage(String type) {
        this();
        this.type = type;
    }

    public Storage(String type, String host) {
        this(type);
        this.host = host;
    }
}
