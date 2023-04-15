package com.netgrif.application.engine.workflow.domain.triggers;

import com.netgrif.application.engine.importer.model.TriggerType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;

import static com.netgrif.application.engine.importer.model.TriggerType.AUTO;

public class AutoTrigger extends Trigger {

    public AutoTrigger() {
        super();
    }

    @Override
    @QueryType(PropertyType.NONE)
    public TriggerType getType() {
        return AUTO;
    }

    @Override
    public Trigger clone() {
        return new AutoTrigger();
    }
}