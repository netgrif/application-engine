package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.domain.User;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.impersonation.domain.Impersonator;
import com.netgrif.application.engine.impersonation.domain.repository.ImpersonatorRepository;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationAuthorizationService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationSessionService;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImpersonationService implements IImpersonationService {

    @Autowired
    private IUserService userService;

    @Autowired
    private IImpersonationSessionService sessionService;

    @Autowired
    private ISecurityContextService securityContextService;

    @Autowired
    private ImpersonatorRepository impersonatorRepository;

    @Autowired
    private IImpersonationAuthorizationService impersonationAuthorizationService;

    @Override
    public LoggedUser impersonate(String impersonatedId) throws ImpersonatedUserHasSessionException {
        LoggedUser loggedUser = userService.getLoggedUser().transformToLoggedUser();
        IUser impersonated = userService.findById(impersonatedId, false);

        List<Case> configs = impersonationAuthorizationService.searchConfigs(loggedUser.getId(), impersonatedId);
        List<Authority> authorities = impersonationAuthorizationService.getAuthorities(configs);
        List<ProcessRole> roles = impersonationAuthorizationService.getRoles(configs);

        LoggedUser impersonatedLogged = impersonated
                .transformToLoggedUser()
                .transformToImpersonatedLoggedUser(authorities, roles.stream().map(ProcessRole::getStringId).collect(Collectors.toList()));

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
        LoggedUser loggedUser = endImpersonation(userService.getLoggedUserFromContext());
        securityContextService.saveToken(loggedUser.getId());
        securityContextService.reloadSecurityContext(loggedUser);
        return loggedUser;
    }

    @Override
    public LoggedUser endImpersonation(LoggedUser loggedUser) {
        LoggedUser impersonated = loggedUser.getImpersonated();
        removeImpersonatedId(loggedUser);
        loggedUser.clearImpersonated();
        log.info(loggedUser.getFullName() + " has stopped impersonating user " + impersonated.getFullName());
        return loggedUser;
    }

    protected void updateImpersonatedId(LoggedUser loggedUser, String id) {
        removeImpersonatedId(loggedUser);
        impersonatorRepository.save(new Impersonator(loggedUser.getId(), id, LocalDateTime.now()));
    }

    protected void removeImpersonatedId(LoggedUser loggedUser) {
        impersonatorRepository.deleteById(loggedUser.getId());
    }
}
