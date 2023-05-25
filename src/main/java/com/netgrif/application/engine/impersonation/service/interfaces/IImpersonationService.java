package com.netgrif.application.engine.impersonation.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.impersonation.domain.Impersonator;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.workflow.domain.Case;

import java.util.List;
import java.util.Optional;

public interface IImpersonationService {

    LoggedUser impersonateUser(String impersonatedId) throws ImpersonatedUserHasSessionException;

    LoggedUser impersonateByConfig(String configId) throws ImpersonatedUserHasSessionException;

    Optional<Impersonator> findImpersonator(String impersonatorId);

    void removeImpersonatorByImpersonated(String impersonatedId);

    void removeImpersonator(String impersonatorId);

    LoggedUser endImpersonation();

    LoggedUser endImpersonation(LoggedUser impersonator);

    void onSessionDestroy(LoggedUser impersonator);

    IUser reloadImpersonatedUserRoles(IUser impersonated, String impersonatorId);

    IUser applyRolesAndAuthorities(IUser impersonated, String impersonatorId, List<Case> configs);
}
