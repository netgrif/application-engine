package com.netgrif.application.engine.impersonation.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.impersonation.domain.Impersonator;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.objects.workflow.domain.Case;

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

    AbstractUser reloadImpersonatedUserRoles(AbstractUser impersonated, String impersonatorId);

    AbstractUser applyRolesAndAuthorities(AbstractUser impersonated, String impersonatorId, List<Case> configs);
}
