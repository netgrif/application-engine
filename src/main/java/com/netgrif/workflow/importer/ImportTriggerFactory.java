package com.netgrif.workflow.importer;

import com.netgrif.workflow.importer.model.ImportTrigger;
import com.netgrif.workflow.workflow.domain.triggers.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public class ImportTriggerFactory {

    private Importer importer;

    public ImportTriggerFactory(Importer importer) {
        this.importer = importer;
    }

    public Trigger buildTrigger(ImportTrigger trigger) throws IllegalArgumentException, DateTimeParseException {
        switch (Trigger.Type.fromString(trigger.getType())) {
            case AUTO:
                return buildAutoTrigger();
            case TIME:
                return buildTimeTrigger(trigger.getContent());
            case USER:
                return buildUserTrigger();
            case MESSAGE:
                return buildMessageTrigger();
            default:
                throw new IllegalArgumentException(trigger.getType() + " is not a valid Trigger type");
        }
    }

    private AutoTrigger buildAutoTrigger() {
        return new AutoTrigger();
    }

    private TimeTrigger buildTimeTrigger(String time) throws DateTimeParseException {
        try {
            LocalDateTime.parse(time);
            return new DateTimeTrigger(time);
        } catch (DateTimeParseException ignored) {
            return new DelayTimeTrigger(time);
        }
    }

    private MessageTrigger buildMessageTrigger() {
        return new MessageTrigger();
    }

    private UserTrigger buildUserTrigger() {
        return new UserTrigger();
    }
}