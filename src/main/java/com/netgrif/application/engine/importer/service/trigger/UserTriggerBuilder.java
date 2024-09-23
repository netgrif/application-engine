package com.netgrif.application.engine.importer.service.trigger;

import com.netgrif.application.engine.importer.model.TriggerType;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import com.netgrif.application.engine.workflow.domain.triggers.UserTrigger;
import org.springframework.stereotype.Component;

@Component
public class UserTriggerBuilder extends TriggerBuilder {

    @Override
    public Trigger build(com.netgrif.application.engine.importer.model.Trigger trigger) {
        return new UserTrigger();
    }

    @Override
    public TriggerType getType() {
        return TriggerType.USER;
    }
}
