package com.netgrif.application.engine.objects.petrinet.domain;

import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.petrinet.domain.version.Version;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
public class PetriNetSearch {

    private String importId;

    private String identifier;

    private String title;

    private String defaultCaseName;

    private Boolean defaultVersion;

    private String initials;

    private List<String> group;

    private Version version;

    private ActorRef author;

    private List<String> roles;

    private List<String> negativeViewRoles;

    private Map<String, String> tags;

    private String search;
}
