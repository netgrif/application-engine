package com.netgrif.workflow.workflow.domain.triggers;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public abstract class TimeTrigger extends Trigger {

    @Getter
    @Setter
    protected LocalDateTime startDate;

    protected TimeTrigger() {
        this._id = new ObjectId();
    }
}