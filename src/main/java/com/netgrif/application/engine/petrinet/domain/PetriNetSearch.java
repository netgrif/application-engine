package com.netgrif.application.engine.petrinet.domain;

import com.netgrif.application.engine.auth.domain.Author;
import com.netgrif.application.engine.workflow.domain.Version;
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

    private String initials;

    private List<String> group;

    private Version version;

    private Author author;

    private List<String> negativeViewRoles;

    private Map<String, String> tags;
}
