package com.netgrif.application.engine.mapper.filters;

import com.netgrif.application.engine.workflow.domain.DataFieldBehavior;

public class DataFieldBehaviorFilter {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return true;
        }
        return !((DataFieldBehavior) obj).hasNonDefaultBehaviourSet();
    }
}
