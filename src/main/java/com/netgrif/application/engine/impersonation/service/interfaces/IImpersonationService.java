package com.netgrif.application.engine.impersonation.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.workflow.domain.Case;

import java.util.List;

public interface IImpersonationService {

    LoggedUser impersonateUser(String impersonatedId) throws ImpersonatedUserHasSessionException;

    LoggedUser impersonateByConfig(String configId) throws ImpersonatedUserHasSessionException;

    LoggedUser endImpersonation();

    void endImpersonation(String impersonatedId);

    void endImpersonator(String impersonatorId);

    LoggedUser endImpersonation(LoggedUser loggedUser);

    IUser reloadImpersonatedUserRoles(IUser impersonated, String impersonatorId);

    IUser applyRolesAndAuthorities(IUser impersonated, String impersonatorId, List<Case> configs);
}
