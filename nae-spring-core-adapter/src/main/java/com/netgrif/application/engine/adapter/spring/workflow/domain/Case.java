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

import java.time.LocalDateTime;
import java.util.*;

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

    @org.springframework.data.mongodb.core.mapping.Field("activePlaces")
    @JsonIgnore
    @Override
    public Map<String, Integer> getActivePlaces() {
        return super.getActivePlaces();
    }

    @JsonIgnore
    @Override
    public Map<String, Integer> getConsumedTokens() {
        return super.getConsumedTokens();
    }

    @JsonIgnore
    @Override
    public Map<String, DataField> getDataSet() {
        return super.getDataSet();
    }

    @JsonIgnore
    @Override
    public Set<TaskPair> getTasks() {
        return super.getTasks();
    }

    @JsonIgnore
    @Override
    public Set<String> getEnabledRoles() {
        return super.getEnabledRoles();
    }

    @Override
    public List<Field<?>> getImmediateData() {
        return super.getImmediateData();
    }

    @JsonIgnore
    @Override
    public Set<String> getImmediateDataFields() {
        return super.getImmediateDataFields();
    }

    @LastModifiedDate
    @Override
    public LocalDateTime getLastModified() {
        return super.getLastModified();
    }

    @JsonIgnore
    @Transient
    @Override
    public PetriNet getPetriNet() {
        return super.getPetriNet();
    }

    @JsonIgnore
    @Override
    public List<String> getViewUserRefs() {
        return super.getViewUserRefs();
    }

    @JsonIgnore
    @Override
    public List<String> getViewUsers() {
        return super.getViewUsers();
    }

    @Override
    public ActorRef getAuthor() {
        return super.getAuthor();
    }
}
