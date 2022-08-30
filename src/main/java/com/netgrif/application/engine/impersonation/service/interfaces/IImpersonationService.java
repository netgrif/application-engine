package com.netgrif.application.engine.impersonation.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IImpersonationService {

    LoggedUser impersonate(String impersonatedId) throws ImpersonatedUserHasSessionException;

    LoggedUser endImpersonation();

    LoggedUser endImpersonation(LoggedUser loggedUser);
}
