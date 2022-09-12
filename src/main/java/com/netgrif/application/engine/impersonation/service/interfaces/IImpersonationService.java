package com.netgrif.application.engine.impersonation.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;

public interface IImpersonationService {

    LoggedUser impersonate(String impersonatedId) throws ImpersonatedUserHasSessionException;

    LoggedUser endImpersonation();

    void endImpersonation(String impersonatedId);

    LoggedUser endImpersonation(LoggedUser loggedUser);

    IUser applyRolesAndAuthorities(IUser impersonated, String impersonatorId);
}
