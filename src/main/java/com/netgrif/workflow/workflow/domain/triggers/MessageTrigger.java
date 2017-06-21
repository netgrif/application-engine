package com.netgrif.workflow.workflow.domain.triggers;

import org.bson.types.ObjectId;

public class MessageTrigger extends Trigger {

    public MessageTrigger() {
        this._id = new ObjectId();
    }

    @Override
    public Trigger clone() {
        return null;
    }
}