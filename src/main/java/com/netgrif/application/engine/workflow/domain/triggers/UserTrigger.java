package com.netgrif.application.engine.workflow.domain.triggers;

import com.netgrif.application.engine.importer.model.TriggerType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;

import static com.netgrif.application.engine.importer.model.TriggerType.USER;

public class UserTrigger extends Trigger {

    public UserTrigger() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public TriggerType getType() {
        return USER;
    }

    @Override
    public Trigger clone() {
        return new UserTrigger();
    }
}