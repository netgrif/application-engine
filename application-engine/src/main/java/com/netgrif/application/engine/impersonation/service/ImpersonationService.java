package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.adapter.spring.auth.domain.AuthorityImpl;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.configuration.properties.ImpersonationProperties;
import com.netgrif.application.engine.objects.event.RunPhase;
import com.netgrif.application.engine.objects.event.events.user.ImpersonationEvent;
import com.netgrif.application.engine.impersonation.domain.Impersonator;
import com.netgrif.application.engine.impersonation.domain.repository.ImpersonatorRepository;
import com.netgrif.application.engine.impersonation.exceptions.ImpersonatedUserHasSessionException;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationAuthorizationService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationSessionService;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.security.service.ISecurityContextService;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
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
    protected UserService userService;

    @Autowired
    protected ApplicationEventPublisher publisher;

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
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(userService.getLoggedUser());
        AbstractUser impersonated = userService.findById(impersonatedId, null);

        List<Case> configs = impersonationAuthorizationService.searchConfigs(loggedUser.getStringId(), impersonated.getStringId());
        LoggedUser impersonatedLogged = ActorTransformer.toLoggedUser(applyRolesAndAuthorities(impersonated, loggedUser.getStringId(), configs));

        return doImpersonate(loggedUser, impersonatedLogged, configs);
    }

    @Override
    public LoggedUser impersonateByConfig(String configId) throws ImpersonatedUserHasSessionException {
        if (!properties.isEnabled()) {
            throw new IllegalArgumentException("Impersonation is not enabled in app properties");
        }
        Case config = impersonationAuthorizationService.getConfig(configId);
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(userService.getLoggedUser());
        AbstractUser impersonated = userService.findById(impersonationAuthorizationService.getImpersonatedUserId(config), null);

        LoggedUser impersonatedLogged = ActorTransformer.toLoggedUser(applyRolesAndAuthorities(impersonated, loggedUser.getStringId(), Collections.singletonList(config)));
        return doImpersonate(loggedUser, impersonatedLogged, Collections.singletonList(config));
    }

    protected LoggedUser doImpersonate(LoggedUser loggedUser, LoggedUser impersonatedLogged, List<Case> configs) throws ImpersonatedUserHasSessionException {
        if (sessionService.existsSession(impersonatedLogged.getUsername())) {
            throw new ImpersonatedUserHasSessionException(impersonatedLogged, false);

        } else if (sessionService.isImpersonated(impersonatedLogged.getStringId())) {
            throw new ImpersonatedUserHasSessionException(impersonatedLogged, true);
        }
        updateImpersonatedId(loggedUser, impersonatedLogged.getStringId(), configs);
        // TODO: impersonation
//        loggedUser.impersonate(impersonatedLogged);
        securityContextService.saveToken(loggedUser.getStringId());
        securityContextService.reloadSecurityContext(loggedUser);
        log.info(loggedUser.getName() + " has just impersonated user " + impersonatedLogged.getName());
        publisher.publishEvent(new ImpersonationEvent(loggedUser, impersonatedLogged, RunPhase.START));
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
        // TODO: impersonation
//        LoggedUser impersonated = impersonator.getImpersonated();
        LoggedUser impersonated = impersonator;
        removeImpersonator(impersonator.getStringId());
        // TODO: impersonation
//        impersonator.clearImpersonated();
        log.info(impersonator.getName() + " has stopped impersonating user " + impersonated.getName());
        securityContextService.saveToken(impersonator.getStringId());
        securityContextService.reloadSecurityContext(impersonator);
        publisher.publishEvent(new ImpersonationEvent(impersonator, impersonated, RunPhase.STOP));
        return impersonator;
    }

    @Override
    public void onSessionDestroy(LoggedUser impersonator) {
        removeImpersonator(impersonator.getStringId());
        // TODO: impersonation
//        log.info(impersonator.getFullName() + " has logged out and stopped impersonating user " + impersonator.getImpersonated().getFullName());
        log.info(impersonator.getName() + " has logged out and stopped impersonating user " + impersonator.getName());
        // TODO: impersonation
//        publisher.publishEvent(new ImpersonationEvent(impersonator, impersonator.getImpersonated(), RunPhase.STOP));
        publisher.publishEvent(new ImpersonationEvent(impersonator, impersonator, RunPhase.STOP));
    }

    @Override
    public AbstractUser reloadImpersonatedUserRoles(AbstractUser impersonated, String impersonatorId) {
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
    public AbstractUser applyRolesAndAuthorities(AbstractUser impersonated, String impersonatorId, List<Case> configs) {
        if ((Boolean) userService.findById(impersonatorId, null).getAuthoritySet().contains(new AuthorityImpl(Authority.admin))) {
            return impersonated;
        }
        List<Authority> authorities = impersonationAuthorizationService.getAuthorities(configs, impersonated);
        List<ProcessRole> roles = impersonationAuthorizationService.getRoles(configs, impersonated);

        impersonated.setAuthoritySet(new HashSet<>(authorities));
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
        removeImpersonator(loggedUser.getStringId());
        impersonatorRepository.save(new Impersonator(loggedUser.getStringId(), id,
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
