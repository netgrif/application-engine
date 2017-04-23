package com.fmworkflow.workflow.domain;

import org.bson.types.ObjectId;

public class AutoTrigger extends Trigger {
    // TODO: 23. 4. 2017 can fire multiple times?


    public AutoTrigger() {
        this._id = new ObjectId();
    }
}