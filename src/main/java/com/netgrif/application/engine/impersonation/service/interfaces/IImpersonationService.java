package com.netgrif.application.engine.impersonation.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.impersonation.domain.Impersonator;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.workflow.domain.Case;

import java.util.List;
import java.util.Optional;

public interface IImpersonationService {

    Identity impersonateUser(String impersonatedId) throws ImpersonatedUserHasSessionException;

    Identity impersonateByConfig(String configId) throws ImpersonatedUserHasSessionException;

    Optional<Impersonator> findImpersonator(String impersonatorId);

    void removeImpersonatorByImpersonated(String impersonatedId);

    void removeImpersonator(String impersonatorId);

    Identity endImpersonation();

    Identity endImpersonation(Identity impersonator);

    void onSessionDestroy(Identity impersonator);

    IUser reloadImpersonatedUserRoles(IUser impersonated, String impersonatorId);

    IUser applyRolesAndAuthorities(IUser impersonated, String impersonatorId, List<Case> configs);
}
