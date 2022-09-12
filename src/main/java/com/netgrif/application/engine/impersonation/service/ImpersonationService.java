package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
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
import java.util.HashSet;
import java.util.List;

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

        LoggedUser impersonatedLogged = applyRolesAndAuthorities(impersonated, loggedUser.getId()).transformToLoggedUser();

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
        return endImpersonation(userService.getLoggedUserFromContext());
    }

    @Override
    public void endImpersonation(String impersonatedId) {
        impersonatorRepository.findByImpersonatedId(impersonatedId).ifPresent(impersonatorRepository::delete);
    }

    @Override
    public LoggedUser endImpersonation(LoggedUser impersonator) {
        LoggedUser impersonated = impersonator.getImpersonated();
        removeImpersonatedId(impersonator);
        impersonator.clearImpersonated();
        log.info(impersonator.getFullName() + " has stopped impersonating user " + impersonated.getFullName());
        securityContextService.saveToken(impersonator.getId());
        securityContextService.reloadSecurityContext(impersonator);
        return impersonator;
    }

    @Override
    public IUser applyRolesAndAuthorities(IUser impersonated, String impersonatorId) {
        List<Case> configs = impersonationAuthorizationService.searchConfigs(impersonatorId, impersonated.getStringId());
        List<Authority> authorities = impersonationAuthorizationService.getAuthorities(configs);
        List<ProcessRole> roles = impersonationAuthorizationService.getRoles(configs);

        impersonated.setAuthorities(new HashSet<>(authorities));
        impersonated.setProcessRoles(new HashSet<>(roles));

        return impersonated;
    }

    protected void updateImpersonatedId(LoggedUser loggedUser, String id) {
        removeImpersonatedId(loggedUser);
        impersonatorRepository.save(new Impersonator(loggedUser.getId(), id, LocalDateTime.now()));
    }

    protected void removeImpersonatedId(LoggedUser loggedUser) {
        impersonatorRepository.deleteById(loggedUser.getId());
    }
}
