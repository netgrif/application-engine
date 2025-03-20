package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.service.interfaces.IUserService;
import com.netgrif.application.engine.authorization.domain.Role;
import com.netgrif.application.engine.configuration.properties.ImpersonationProperties;
import com.netgrif.application.engine.history.domain.impersonationevents.ImpersonationEndEventLog;
import com.netgrif.application.engine.history.service.IHistoryService;
import com.netgrif.application.engine.impersonation.domain.Impersonator;
import com.netgrif.application.engine.impersonation.domain.repository.ImpersonatorRepository;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationAuthorizationService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationSessionService;
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
    public Identity impersonateUser(String impersonatedId) throws ImpersonatedUserHasSessionException {
        if (!properties.isEnabled()) {
            throw new IllegalArgumentException("Impersonation is not enabled in app properties");
        }
        Identity identity = userService.getLoggedUser().transformToLoggedUser();
        IUser impersonated = userService.findById(impersonatedId);

        List<Case> configs = impersonationAuthorizationService.searchConfigs(identity.getId(), impersonated.getStringId());
        Identity impersonatedLogged = applyRolesAndAuthorities(impersonated, identity.getId(), configs).transformToLoggedUser();

        return doImpersonate(identity, impersonatedLogged, configs);
    }

    @Override
    public Identity impersonateByConfig(String configId) throws ImpersonatedUserHasSessionException {
        if (!properties.isEnabled()) {
            throw new IllegalArgumentException("Impersonation is not enabled in app properties");
        }
        Case config = impersonationAuthorizationService.getConfig(configId);
        Identity identity = userService.getLoggedUser().transformToLoggedUser();
        IUser impersonated = userService.findById(impersonationAuthorizationService.getImpersonatedUserId(config));

        Identity impersonatedLogged = applyRolesAndAuthorities(impersonated, identity.getId(), Collections.singletonList(config)).transformToLoggedUser();
        return doImpersonate(identity, impersonatedLogged, Collections.singletonList(config));
    }

    protected Identity doImpersonate(Identity identity, Identity impersonatedLogged, List<Case> configs) throws ImpersonatedUserHasSessionException {
        if (sessionService.existsSession(impersonatedLogged.getUsername())) {
            throw new ImpersonatedUserHasSessionException(impersonatedLogged, false);

        } else if (sessionService.isImpersonated(impersonatedLogged.getId())) {
            throw new ImpersonatedUserHasSessionException(impersonatedLogged, true);
        }
        updateImpersonatedId(identity, impersonatedLogged.getId(), configs);
        identity.impersonate(impersonatedLogged);
        securityContextService.saveToken(identity.getId());
        securityContextService.reloadSecurityContext(identity);
        log.info(identity.getFullName() + " has just impersonated user " + impersonatedLogged.getFullName());
        // todo 2058
//        historyService.save(
//                new ImpersonationStartEventLog(loggedUser.getId(), impersonatedLogged.getId(),
//                        new ArrayList<>(impersonatedLogged.getRoles()),
//                        impersonatedLogged.getAuthorities().stream().map(au -> ((Authority) au).getStringId()).collect(Collectors.toList()))
//        );
        return identity;
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
    public Identity endImpersonation() {
        return endImpersonation(userService.getLoggedUserFromContext());
    }

    @Override
    public Identity endImpersonation(Identity impersonator) {
        Identity impersonated = impersonator.getImpersonated();
        removeImpersonator(impersonator.getId());
        impersonator.clearImpersonated();
        log.info(impersonator.getFullName() + " has stopped impersonating user " + impersonated.getFullName());
        securityContextService.saveToken(impersonator.getId());
        securityContextService.reloadSecurityContext(impersonator);
        historyService.save(new ImpersonationEndEventLog(impersonator.getId(), impersonated.getId()));
        return impersonator;
    }

    @Override
    public void onSessionDestroy(Identity impersonator) {
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
        if (userService.findById(impersonatorId).transformToLoggedUser().isAdmin()) {
            return impersonated;
        }
        List<Authority> authorities = impersonationAuthorizationService.getAuthorities(configs, impersonated);
        List<Role> roles = impersonationAuthorizationService.getRoles(configs, impersonated);

        impersonated.setAuthorities(new HashSet<>(authorities));
        // todo 2058
//        impersonated.setRoles(new HashSet<>(roles));

        return impersonated;
    }

    protected void updateImpersonatedId(Identity identity, String id, List<Case> configs) {
        Map<Case, LocalDateTime> configTimeMap = new HashMap<>();
        configs.forEach((config) -> configTimeMap.put(config, getConfigValidToTime(config)));
        Optional<Map.Entry<Case, LocalDateTime>> earliestEndingConfig = configTimeMap
                .entrySet().stream()
                .filter(it -> it.getValue() != null)
                .min(Map.Entry.comparingByValue());
            updateImpersonatedId(identity, id, configs, earliestEndingConfig.map(Map.Entry::getValue).orElse(null));
    }

    protected void updateImpersonatedId(Identity identity, String id, List<Case> configs, LocalDateTime validUntil) {
        removeImpersonator(identity.getId());
        impersonatorRepository.save(new Impersonator(identity.getId(), id,
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
