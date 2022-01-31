package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.workflow.domain.triggers.*;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeParseException;

@Component
public class TriggerFactory {

    public Trigger buildTrigger(com.netgrif.application.engine.importer.model.Trigger trigger) throws IllegalArgumentException, DateTimeParseException {
        switch (trigger.getType()) {
            case AUTO:
                return buildAutoTrigger();
            case TIME:
                return buildTimeTrigger(trigger);
            case USER:
                return buildUserTrigger();
            default:
                throw new IllegalArgumentException(trigger.getType() + " is not a valid Trigger type");
        }
    }

    private AutoTrigger buildAutoTrigger() {
        return new AutoTrigger();
    }

    private TimeTrigger buildTimeTrigger(com.netgrif.application.engine.importer.model.Trigger trigger) throws DateTimeParseException {
        if (trigger.getDelay() != null) {
            return new DelayTimeTrigger(trigger.getDelay().toString());
        } else if (trigger.getExact() != null) {
            return new DateTimeTrigger(trigger.getExact().toString());
        }
        throw new IllegalArgumentException("Unsupported time trigger value");
    }

    private UserTrigger buildUserTrigger() {
        return new UserTrigger();
    }
}