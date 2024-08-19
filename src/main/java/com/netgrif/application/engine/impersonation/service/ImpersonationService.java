package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.configuration.properties.ImpersonationProperties;
import com.netgrif.application.engine.history.domain.impersonationevents.ImpersonationEndEventLog;
import com.netgrif.application.engine.history.domain.impersonationevents.ImpersonationStartEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ImpersonationService implements IImpersonationService {

    @Autowired
    protected ImpersonationProperties properties;

    @Autowired
    protected IUserService userService;

    @Autowired
    protected IHistoryService historyService;

    @Autowired
    protected IImpersonationSessionService sessionService;

    @Autowired
    protected ISecurityContextService securityContextService;

    @Autowired
    protected ImpersonatorRepository impersonatorRepository;

    @Autowired
    protected IImpersonationAuthorizationService impersonationAuthorizationService;

    @Override
    public LoggedUser impersonateUser(String impersonatedId) throws ImpersonatedUserHasSessionException {
        if (!properties.isEnabled()) {
            throw new IllegalArgumentException("Impersonation is not enabled in app properties");
        }
        LoggedUser loggedUser = userService.getLoggedUser().transformToLoggedUser();
        IUser impersonated = userService.findById(impersonatedId, false);

        List<Case> configs = impersonationAuthorizationService.searchConfigs(loggedUser.getId(), impersonated.getStringId());
        LoggedUser impersonatedLogged = applyRolesAndAuthorities(impersonated, loggedUser.getId(), configs).transformToLoggedUser();

        return doImpersonate(loggedUser, impersonatedLogged, configs);
    }

    @Override
    public LoggedUser impersonateByConfig(String configId) throws ImpersonatedUserHasSessionException {
        if (!properties.isEnabled()) {
            throw new IllegalArgumentException("Impersonation is not enabled in app properties");
        }
        Case config = impersonationAuthorizationService.getConfig(configId);
        LoggedUser loggedUser = userService.getLoggedUser().transformToLoggedUser();
        IUser impersonated = userService.findById(impersonationAuthorizationService.getImpersonatedUserId(config), false);

        LoggedUser impersonatedLogged = applyRolesAndAuthorities(impersonated, loggedUser.getId(), Collections.singletonList(config)).transformToLoggedUser();
        return doImpersonate(loggedUser, impersonatedLogged, Collections.singletonList(config));
    }

    protected LoggedUser doImpersonate(LoggedUser loggedUser, LoggedUser impersonatedLogged, List<Case> configs) throws ImpersonatedUserHasSessionException {
        if (sessionService.existsSession(impersonatedLogged.getUsername())) {
            throw new ImpersonatedUserHasSessionException(impersonatedLogged, false);

        } else if (sessionService.isImpersonated(impersonatedLogged.getId())) {
            throw new ImpersonatedUserHasSessionException(impersonatedLogged, true);
        }
        updateImpersonatedId(loggedUser, impersonatedLogged.getId(), configs);
        loggedUser.impersonate(impersonatedLogged);
        securityContextService.saveToken(loggedUser.getId());
        securityContextService.reloadSecurityContext(loggedUser);
        log.info(loggedUser.getFullName() + " has just impersonated user " + impersonatedLogged.getFullName());
        historyService.save(
                new ImpersonationStartEventLog(loggedUser.getId(), impersonatedLogged.getId(),
                        new ArrayList<>(impersonatedLogged.getProcessRoles()),
                        impersonatedLogged.getAuthorities().stream().map(au -> ((Authority) au).getStringId()).collect(Collectors.toList()))
        );
        return loggedUser;
    }

    @Override
    public Optional<Impersonator> findImpersonator(String impersonatorId) {
        return impersonatorRepository.findById(impersonatorId);
    }

    @Override
    public void removeImpersonatorByImpersonated(String impersonatedId) {
        impersonatorRepository.findByImpersonatedId(impersonatedId).ifPresent(impersonatorRepository::delete);
    }

    @Override
    public void removeImpersonator(String impersonatorId) {
        impersonatorRepository.deleteById(impersonatorId);
    }

    @Override
    public LoggedUser endImpersonation() {
        return endImpersonation(userService.getLoggedUserFromContext());
    }

    @Override
    public LoggedUser endImpersonation(LoggedUser impersonator) {
        LoggedUser impersonated = impersonator.getImpersonated();
        removeImpersonator(impersonator.getId());
        impersonator.clearImpersonated();
        log.info(impersonator.getFullName() + " has stopped impersonating user " + impersonated.getFullName());
        securityContextService.saveToken(impersonator.getId());
        securityContextService.reloadSecurityContext(impersonator);
        historyService.save(new ImpersonationEndEventLog(impersonator.getId(), impersonated.getId()));
        return impersonator;
    }

    @Override
    public void onSessionDestroy(LoggedUser impersonator) {
        removeImpersonator(impersonator.getId());
        log.info(impersonator.getFullName() + " has logged out and stopped impersonating user " + impersonator.getImpersonated().getFullName());
        historyService.save(new ImpersonationEndEventLog(impersonator.getId(), impersonator.getImpersonated().getId()));
    }

    @Override
    public IUser reloadImpersonatedUserRoles(IUser impersonated, String impersonatorId) {
        Optional<Impersonator> context = impersonatorRepository.findByImpersonatedId(impersonated.getStringId());
        if (context.isPresent()) {
            List<Case> configs = context.get().getConfigIds().stream()
                    .map(id -> impersonationAuthorizationService.getConfig(id))
                    .collect(Collectors.toList());
            return applyRolesAndAuthorities(impersonated, impersonatorId, configs);
        }
        return impersonated;
    }

    @Override
    public IUser applyRolesAndAuthorities(IUser impersonated, String impersonatorId, List<Case> configs) {
        if (userService.findById(impersonatorId, true).transformToLoggedUser().isAdmin()) {
            return impersonated;
        }
        List<Authority> authorities = impersonationAuthorizationService.getAuthorities(configs, impersonated);
        List<ProcessRole> roles = impersonationAuthorizationService.getRoles(configs, impersonated);

        impersonated.setAuthorities(new HashSet<>(authorities));
        impersonated.setProcessRoles(new HashSet<>(roles));

        return impersonated;
    }

    protected void updateImpersonatedId(LoggedUser loggedUser, String id, List<Case> configs) {
        Map<Case, LocalDateTime> configTimeMap = new HashMap<>();
        configs.forEach((config) -> configTimeMap.put(config, getConfigValidToTime(config)));
        Optional<Map.Entry<Case, LocalDateTime>> earliestEndingConfig = configTimeMap
                .entrySet().stream()
                .filter(it -> it.getValue() != null)
                .min(Map.Entry.comparingByValue());
            updateImpersonatedId(loggedUser, id, configs, earliestEndingConfig.map(Map.Entry::getValue).orElse(null));
    }

    protected void updateImpersonatedId(LoggedUser loggedUser, String id, List<Case> configs, LocalDateTime validUntil) {
        removeImpersonator(loggedUser.getId());
        impersonatorRepository.save(new Impersonator(loggedUser.getId(), id,
                configs.stream().map(Case::getStringId).collect(Collectors.toList()),
                LocalDateTime.now(), validUntil));
    }

    protected LocalDateTime getConfigValidToTime(Case config) {
        LocalDateTime limitTime = null;
        if (config != null) {
            limitTime = impersonationAuthorizationService.getValidUntil(config);
        }
        return limitTime;
    }
}
