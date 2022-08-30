package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationSessionService;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ImpersonationService implements IImpersonationService {

    @Autowired
    private IUserService userService;

    @Autowired
    private IImpersonationSessionService sessionService;

    @Autowired
    private ISecurityContextService securityContextService;

    @Override
    public LoggedUser impersonate(String impersonatedId) throws ImpersonatedUserHasSessionException {
        LoggedUser loggedUser = userService.getLoggedUser().transformToLoggedUser();

        IUser impersonated = userService.findById(impersonatedId, false);
        LoggedUser impersonatedLogged = impersonated.transformToLoggedUser();
        if (sessionService.existsSession(impersonatedLogged.getUsername())) {
            throw new ImpersonatedUserHasSessionException(impersonatedLogged, false);

        } else if (sessionService.isImpersonated(impersonatedLogged.getId())) {
            throw new ImpersonatedUserHasSessionException(impersonatedLogged, true);
        }

        updateImpersonatedId(loggedUser, impersonated.getStringId());
        loggedUser.impersonate(impersonatedLogged);
        securityContextService.saveToken(loggedUser.getId());
        securityContextService.reloadSecurityContext(loggedUser);
        log.info(loggedUser.getFullName() + " has just impersonated user " + impersonated.getFullName());
        return loggedUser;
    }

    @Override
    public LoggedUser endImpersonation() {
        LoggedUser loggedUser = endImpersonation(userService.getLoggedUser().transformToLoggedUser());
        securityContextService.reloadSecurityContext(loggedUser);
        return loggedUser;
    }

    @Override
    public LoggedUser endImpersonation(LoggedUser loggedUser) {
        LoggedUser impersonated = loggedUser.getImpersonated();
        updateImpersonatedId(loggedUser, null);
        loggedUser.clearImpersonated();
        log.info(loggedUser.getFullName() + " has stopped impersonating user " + impersonated.getFullName());
        return loggedUser;
    }

    private void updateImpersonatedId(LoggedUser loggedUser, String id) {
        // TODO 1678 redis
    }
}
