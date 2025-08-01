package com.netgrif.application.engine.menu.services.interfaces;

import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardItemBody;

public interface DashboardItemService {

    Case getOrCreate(DashboardItemBody body) throws TransitionNotExecutableException;

    Case update(Case itemCase, DashboardItemBody body) throws TransitionNotExecutableException;

    Case findById(String identifier);
}
