package com.netgrif.application.engine.adapter.spring.workflow.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.netgrif.application.engine.objects.auth.domain.ActorRef;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.*;
import com.netgrif.application.engine.objects.workflow.domain.DataField;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.objects.workflow.domain.TaskPair;
import com.querydsl.core.annotations.QueryEntity;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.*;

@Document
@QueryEntity
public class Case extends com.netgrif.application.engine.objects.workflow.domain.Case {

    public Case() {
        super();
    }

    public Case(PetriNet petriNet) {
        super(petriNet);
    }

    @Id
    @Override
    public ProcessResourceId get_id() {
        return super.get_id();
    }

    @Override
    @JsonIgnore
    @org.springframework.data.mongodb.core.mapping.Field("activePlaces")
    public Map<String, Integer> getActivePlaces() {
        return super.getActivePlaces();
    }

    @Override
    @JsonIgnore
    public Map<String, Integer> getConsumedTokens() {
        return super.getConsumedTokens();
    }

    @Override
    @JsonIgnore
    public Map<String, DataField> getDataSet() {
        return super.getDataSet();
    }

    @Override
    public Set<TaskPair> getTasks() {
        return super.getTasks();
    }

    @Override
    @JsonIgnore
    public Set<String> getEnabledRoles() {
        return super.getEnabledRoles();
    }


    @Override
    @Transient
    public List<Field<?>> getImmediateData() {
        return super.getImmediateData();
    }

    @Override
    @JsonIgnore
    public Set<String> getImmediateDataFields() {
        return super.getImmediateDataFields();
    }

    @Override
    @LastModifiedDate
    public LocalDateTime getLastModified() {
        return super.getLastModified();
    }

    @Override
    @Transient
    @JsonIgnore
    public PetriNet getPetriNet() {
        return super.getPetriNet();
    }

    @Override
    @JsonIgnore
    public List<String> getViewActorRefs() {
        return super.getViewActorRefs();
    }

    @Override
    @JsonIgnore
    public List<String> getViewActors() {
        return super.getViewActors();
    }

    @Override
    public ActorRef getAuthor() {
        return super.getAuthor();
    }
}
