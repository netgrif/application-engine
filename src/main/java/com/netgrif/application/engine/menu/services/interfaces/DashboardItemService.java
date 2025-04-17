package com.netgrif.application.engine.menu.services.interfaces;

import com.netgrif.application.engine.menu.domain.dashboard.DashboardItemBody;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.Case;

public interface DashboardItemService {

    Case getOrCreate(DashboardItemBody body) throws TransitionNotExecutableException;

    Case update(Case itemCase, DashboardItemBody body) throws TransitionNotExecutableException;

    Case findById(String identifier);
}
