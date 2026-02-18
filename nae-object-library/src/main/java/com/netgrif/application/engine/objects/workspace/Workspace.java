package com.netgrif.application.engine.objects.workspace;

import com.netgrif.application.engine.objects.annotations.EnsureCollection;
import com.netgrif.application.engine.objects.annotations.Indexed;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** todo javadoc */
@Getter
@EnsureCollection
public abstract class Workspace implements Serializable {

    public static final String FORBIDDEN_ID = "fxhvyihxmgmkpnfwrykmr";
    public static final Pattern idPattern = Pattern.compile("[a-zA-Z0-9_-]{3,}");

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
        // todo 2072 implement test
        if (id == null) {
            throw new IllegalArgumentException("Workspace id cannot be null");
        }
        Matcher matcher = idPattern.matcher(id);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Workspace id is not valid");
        }
        this.id = id;
    }
}
