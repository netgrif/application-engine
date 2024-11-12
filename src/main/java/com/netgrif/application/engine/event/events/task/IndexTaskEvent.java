package com.netgrif.application.engine.event.events.task;

import com.netgrif.application.engine.elastic.domain.ElasticTask;
import com.netgrif.application.engine.event.events.Event;
import lombok.Getter;

@Getter
public class IndexTaskEvent extends Event {

    protected final ElasticTask task;

    public IndexTaskEvent(ElasticTask task) {
        super(task);
        this.task = task;
    }

    public String getMessage() {
        return "IndexTaskEvent: Task [" + task.getStringId() + "] indexed";
    }
}
