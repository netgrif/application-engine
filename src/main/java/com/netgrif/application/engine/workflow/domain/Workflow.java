package com.netgrif.application.engine.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import com.netgrif.application.engine.workflow.domain.arcs.ArcCollection;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class Workflow {
    private UniqueKeyMap<String, Place> places;
    private UniqueKeyMap<String, ArcCollection> arcs;//todo: import id
    private UniqueKeyMap<String, Transition> transitions;

    @JsonIgnore
    private Map<String, Integer> activePlaces;
    @JsonIgnore
    @QueryType(PropertyType.NONE)
    private Map<String, Integer> consumedTokens;

    public Workflow() {
        places = new UniqueKeyMap<>();
        activePlaces = new HashMap<>();
        consumedTokens = new HashMap<>();
        arcs = new UniqueKeyMap<>();
    }
}
