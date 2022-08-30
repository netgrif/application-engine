package com.netgrif.application.engine.impersonation.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IImpersonationAuthorizationService {

    Page<IUser> getConfiguredImpersonationUsers(String query, LoggedUser impersonator, Pageable pageable);

    boolean canImpersonate(LoggedUser impersonator, String userId);

}
