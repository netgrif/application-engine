package com.netgrif.application.engine.authorization.domain;

import com.netgrif.application.engine.workflow.domain.SystemCase;

public interface Actor extends SystemCase {
    String getName();
}
