package com.netgrif.application.engine.menu.services.interfaces;

import com.netgrif.application.engine.menu.domain.dashboard.DashboardManagementBody;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.Case;

public interface DashboardManagementService {
    Case createDashboardManagement(DashboardManagementBody body) throws TransitionNotExecutableException;

    Case updateDashboardManagement(Case managementCase, DashboardManagementBody body) throws TransitionNotExecutableException;

    Case findDashboardManagement(String identifier);

}
