package com.netgrif.application.engine.importer.service.trigger;

import com.netgrif.application.engine.importer.model.TriggerType;
import com.netgrif.application.engine.workflow.domain.triggers.DateTimeTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.DelayTimeTrigger;
import com.netgrif.application.engine.workflow.domain.triggers.Trigger;
import org.springframework.stereotype.Component;

@Component
public class TimeTriggerBuilder extends TriggerBuilder {

    @Override
    public Trigger build(com.netgrif.application.engine.importer.model.Trigger trigger) {
        if (trigger.getDelay() != null) {
            return new DelayTimeTrigger(trigger.getDelay().toString());
        } else if (trigger.getExact() != null) {
            return new DateTimeTrigger(trigger.getExact().toString());
        }
        throw new IllegalArgumentException("Unsupported time trigger value");
    }

    @Override
    public TriggerType getType() {
        return TriggerType.TIME;
    }
}
