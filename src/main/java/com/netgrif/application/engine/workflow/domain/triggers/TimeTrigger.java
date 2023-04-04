package com.netgrif.application.engine.workflow.domain.triggers;

import com.netgrif.application.engine.importer.model.TriggerType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

import static com.netgrif.application.engine.importer.model.TriggerType.TIME;

@Data
public abstract class TimeTrigger extends Trigger {

    private LocalDateTime startDate;
    private String timeString;

    protected TimeTrigger(String timeString) {
        super();
        this.timeString = timeString;
    }

    @Override
    @QueryType(PropertyType.NONE)
    public TriggerType getType() {
        return TIME;
    }
}