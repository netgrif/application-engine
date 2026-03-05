package com.netgrif.application.engine.objects.event.events.task;

import com.netgrif.application.engine.objects.elastic.domain.ElasticTask;
import com.netgrif.application.engine.objects.event.events.Event;
import lombok.Getter;

@Getter
public class IndexTaskEvent extends Event {

    protected final ElasticTask task;

    public IndexTaskEvent(ElasticTask task) {
        super(task,null, getWorkspaceIdFromResource(task));
        this.task = task;
    }

    public String getMessage() {
        return "IndexTaskEvent: Task [%s] indexed".formatted(task.getId());
    }
}
