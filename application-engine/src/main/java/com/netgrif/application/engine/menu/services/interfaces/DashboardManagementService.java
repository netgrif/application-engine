package com.netgrif.application.engine.menu.services.interfaces;

import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.menu.dashboard.DashboardManagementBody;

/**
 * Service interface for managing dashboard management cases.
 */
public interface DashboardManagementService {

    /**
     * Creates a new dashboard management case.
     *
     * @param body the dashboard management configuration
     * @return the created dashboard management case
     * @throws TransitionNotExecutableException if the creation workflow transition fails
     */
    Case createDashboardManagement(DashboardManagementBody body) throws TransitionNotExecutableException;

    /**
     * Updates an existing dashboard management case.
     *
     * @param managementCase the existing dashboard management case to update
     * @param body           the updated dashboard management configuration
     * @return the updated dashboard management case
     * @throws TransitionNotExecutableException if the update workflow transition fails
     */
    Case updateDashboardManagement(Case managementCase, DashboardManagementBody body) throws TransitionNotExecutableException;

    /**
     * Finds a dashboard management case by identifier.
     *
     * @param identifier the dashboard identifier to search for
     * @return the dashboard management case, or null if not found
     */
    Case findDashboardManagement(String identifier);

}
