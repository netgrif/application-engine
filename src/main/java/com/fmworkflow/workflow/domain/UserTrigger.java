package com.fmworkflow.workflow.domain;

import org.bson.types.ObjectId;

public class UserTrigger extends Trigger {

    public UserTrigger() {
        this._id = new ObjectId();
    }
}