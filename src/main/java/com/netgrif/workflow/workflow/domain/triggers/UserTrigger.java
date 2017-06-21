package com.netgrif.workflow.workflow.domain.triggers;

import org.bson.types.ObjectId;

public class UserTrigger extends Trigger {

    public UserTrigger() {
        this._id = new ObjectId();
    }

    @Override
    public Trigger clone() {
        return null;
    }
}