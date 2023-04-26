package com.netgrif.application.engine.mapper.filters;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.FieldLayout;
import com.netgrif.application.engine.workflow.domain.DataFieldBehaviors;

public class DataFieldBehaviorsFilter {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return true;
        }
        return !((DataFieldBehaviors) obj).hasAnyBehaviorOnAnyTransition();
    }
}
