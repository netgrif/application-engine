package com.netgrif.application.engine.mapper.mixins;

import com.fasterxml.jackson.annotation.JsonView;
import com.netgrif.application.engine.mapper.annotation.NaeMixin;
import com.netgrif.application.engine.mapper.views.Views;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.EventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessage;
import org.apache.lucene.queryparser.surround.query.SrndTermQuery;
import org.springframework.beans.factory.annotation.Lookup;

@NaeMixin
public abstract class EventOutcomeWithMessageMixin {

    @Lookup
    public static Class<?> getOriginalType() {
        return EventOutcomeWithMessage.class;
    }

    @JsonView(Views.Root.class)
    public abstract EventOutcome getOutcome();

    @JsonView(Views.Root.class)
    public abstract String getSuccess();

    @JsonView(Views.Root.class)
    public abstract String getError();

    @JsonView(Views.Root.class)
    public abstract SrndTermQuery getData();

}
