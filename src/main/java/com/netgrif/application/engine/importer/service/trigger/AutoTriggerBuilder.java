package com.netgrif.application.engine.importer.service.trigger;

import com.netgrif.application.engine.importer.model.TriggerType;
import com.netgrif.application.engine.workflow.domain.triggers.AutoTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import org.springframework.stereotype.Component;

@Component
public class AutoTriggerBuilder extends TriggerBuilder {

    @Override
    public Trigger build(com.netgrif.application.engine.importer.model.Trigger trigger) {
        return new AutoTrigger();
    }

    @Override
    public TriggerType getType() {
        return TriggerType.AUTO;
    }
}
