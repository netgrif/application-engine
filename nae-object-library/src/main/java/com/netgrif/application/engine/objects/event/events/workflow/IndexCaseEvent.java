package com.netgrif.application.engine.objects.event.events.workflow;

import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.objects.event.events.Event;
import lombok.Getter;

@Getter
public class IndexCaseEvent extends Event {

    protected final ElasticCase elasticCase;

    public IndexCaseEvent(ElasticCase elasticCase) {
        super(elasticCase, getWorkspaceIdFromResource(elasticCase));
        this.elasticCase = elasticCase;
    }

    @Override
    public String getMessage() {
        return "IndexCaseEvent: Case [%s] indexed".formatted(elasticCase.getId());
    }
}
