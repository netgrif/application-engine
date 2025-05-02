package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.workflow.domain.Case;

public interface Actor {
    String getName();
    Case getCase();
}
