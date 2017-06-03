package com.netgrif.workflow.importer;

import com.netgrif.workflow.importer.model.ImportTrigger;
import com.netgrif.workflow.workflow.domain.*;

public class ImportTriggerFactory {
    private Importer importer;

    public ImportTriggerFactory(Importer importer) {
        this.importer = importer;
    }

    public Trigger buildTrigger(ImportTrigger trigger) throws IllegalArgumentException {
        switch (Trigger.Type.fromString(trigger.getType())) {
            case AUTO:
                return buildAutoTrigger();
            case TIME:
                return buildTimeTrigger();
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

    private TimeTrigger buildTimeTrigger() {
        return new TimeTrigger();
    }

    private MessageTrigger buildMessageTrigger() {
        return new MessageTrigger();
    }

    private UserTrigger buildUserTrigger() {
        return new UserTrigger();
    }
}