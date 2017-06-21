package com.netgrif.workflow.workflow.domain.triggers;

import org.bson.types.ObjectId;

public class AutoTrigger extends Trigger {

    public AutoTrigger() {
        this._id = new ObjectId();
    }

    @Override
    public Trigger clone() {
        return null;
    }
}