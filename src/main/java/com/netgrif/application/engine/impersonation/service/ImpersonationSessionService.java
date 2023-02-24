package com.netgrif.application.engine.impersonation.service;

import com.netgrif.application.engine.impersonation.domain.repository.ImpersonatorRepository;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationSessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.security.SpringSessionBackedSessionRegistry;
import org.springframework.stereotype.Service;

import java.util.Collection;

@Service
public class ImpersonationSessionService implements IImpersonationSessionService {

    @Autowired
    protected FindByIndexNameSessionRepository<? extends Session> sessions;

    @Autowired
    protected SpringSessionBackedSessionRegistry<? extends Session> registry;

    @Autowired
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
}
