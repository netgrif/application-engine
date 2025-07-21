package com.netgrif.application.engine.menu.services.interfaces;

import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardManagementBody;

public interface DashboardManagementService {
    Case createDashboardManagement(DashboardManagementBody body) throws TransitionNotExecutableException;

    Case updateDashboardManagement(Case managementCase, DashboardManagementBody body) throws TransitionNotExecutableException;

    Case findDashboardManagement(String identifier);

}
