package com.netgrif.application.engine.adapter.spring.workflow.domain;

import com.netgrif.application.engine.objects.workflow.domain.DataFieldsCollection;

import java.beans.Transient;

public class DataGroup extends com.netgrif.application.engine.objects.petrinet.domain.DataGroup {

    public DataGroup() {
        super();
    }

    public DataGroup(DataGroup group) {
        super(group);
    }

    @Transient
    @Override
    public DataFieldsCollection<?> getFields() {
        return super.getFields();
    }

    @Transient
    @Override
    public String getParentTaskId() {
        return super.getParentTaskId();
    }

    @Transient
    @Override
    public String getParentTransitionId() {
        return super.getParentTransitionId();
    }

    @Transient
    @Override
    public String getParentCaseId() {
        return super.getParentCaseId();
    }

    @Transient
    @Override
    public String getParentTaskRefId() {
        return super.getParentTaskRefId();
    }

    @Transient
    @Override
    public int getNestingLevel() {
        return super.getNestingLevel();
    }
}
