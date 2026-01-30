package com.netgrif.application.engine.objects.workspace;

import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/** todo javadoc */
@Getter
public abstract class Workspace implements Serializable {

    public static final String FORBIDDEN_ID = "JozefMachacJeTlsty";

    @Serial
    private static final long serialVersionUID = -673503926854227584L;

    private final String id;

    @Setter
    private String name;

    @Setter
    private String description;

    @Setter
    private ActorRef owner;

    @Setter
    @Indexed
    private boolean defaultWorkspace;

    public Workspace(String id) {
        // todo 2072 regex validation
        if (id == null) {
            throw new IllegalArgumentException("Workspace id cannot be null");
        }
        this.id = id;
    }
}
