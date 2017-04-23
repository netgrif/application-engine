package com.fmworkflow.workflow.domain;

import org.bson.types.ObjectId;

import java.time.ZonedDateTime;

public class TimeTrigger extends Trigger {
    private ZonedDateTime startDate;

    public TimeTrigger() {
        this._id = new ObjectId();
    }
}