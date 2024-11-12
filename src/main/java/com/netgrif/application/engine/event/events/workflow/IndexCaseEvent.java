package com.netgrif.application.engine.event.events.workflow;

import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.event.events.Event;
import lombok.Getter;

@Getter
public class IndexCaseEvent extends Event {

    protected final ElasticCase elasticCase;

    public IndexCaseEvent(ElasticCase elasticCase) {
        super(elasticCase);
        this.elasticCase = elasticCase;
    }

    @Override
    public String getMessage() {
        return "IndexCaseEvent: Case [" + elasticCase.getStringId() + "] indexed";
    }
}
