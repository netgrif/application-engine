package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.impersonation.domain.repository.ImpersonatorRepository;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ImpersonationSessionService implements IImpersonationSessionService {

    protected FindByIndexNameSessionRepository<? extends Session> sessions;
    protected SpringSessionBackedSessionRegistry<? extends Session> registry;
    protected ImpersonatorRepository impersonatorRepository;

    @Override
    public boolean existsSession(String username) {
        Collection<? extends Session> usersSessions = this.sessions.findByPrincipalName(username).values();
        return usersSessions.stream().anyMatch(session -> !registry.getSessionInformation(session.getId()).isExpired());
    }

    @Override
    public boolean isImpersonated(String userId) {
        return impersonatorRepository.findByImpersonatedId(userId).isPresent();
    }

    @Autowired
    @Lazy
    public void setSessions(FindByIndexNameSessionRepository<? extends Session> sessions) {
        this.sessions = sessions;
    }

    @Autowired
    @Lazy
    public void setRegistry(SpringSessionBackedSessionRegistry<? extends Session> registry) {
        this.registry = registry;
    }

    @Autowired
    @Lazy
    public void setImpersonatorRepository(ImpersonatorRepository impersonatorRepository) {
        this.impersonatorRepository = impersonatorRepository;
    }
}
