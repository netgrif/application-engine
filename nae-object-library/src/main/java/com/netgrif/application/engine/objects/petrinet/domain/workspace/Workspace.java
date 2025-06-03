package com.netgrif.application.engine.objects.petrinet.domain.workspace;

import com.netgrif.application.engine.objects.auth.domain.Author;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Workspace implements Serializable {

    @Serial
    private static final long serialVersionUID = 2708949961379974800L;

    private String id;

    private String name;

    private String description;

    private Author owner;

    private boolean defaultWorkspace;

}