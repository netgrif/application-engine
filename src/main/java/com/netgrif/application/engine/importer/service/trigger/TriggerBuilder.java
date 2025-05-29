package com.netgrif.application.engine.importer.service.trigger;

import com.netgrif.application.engine.importer.model.TriggerType;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;

public abstract class TriggerBuilder {

    public abstract Trigger build(com.netgrif.application.engine.importer.model.Trigger trigger);

    public abstract TriggerType getType();
}
