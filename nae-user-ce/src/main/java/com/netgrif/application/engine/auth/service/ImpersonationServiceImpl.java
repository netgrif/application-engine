package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.Impersonation;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.NotImplementedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Slf4j
public class ImpersonationServiceImpl implements ImpersonationService {

    @Override
    public Impersonation getImpersonation(String impersonationId, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException {
        log.error("Trying to call getImpersonation(String impersonationId, LoggedUser loggedUser) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public Impersonation createImpersonation(Impersonation impersonation, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException {
        log.error("Trying to call createImpersonation(Impersonation impersonation, LoggedUser loggedUser) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public Impersonation updateImpersonation(Impersonation impersonation, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException {
        log.error("Trying to call updateImpersonation(Impersonation impersonation, LoggedUser loggedUser) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public void removeImpersonations(List<String> impersonationIds, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException {
        log.error("Trying to call removeImpersonations(List<String> impersonationIds, LoggedUser loggedUser) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public void removeImpersonation(String impersonationId, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException {
        log.error("Trying to call removeImpersonation(String impersonationId, LoggedUser loggedUser) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public void removeImpersonation(Impersonation impersonation, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException {
        log.error("Trying to call removeImpersonation(Impersonation impersonation, LoggedUser loggedUser) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public Page<Impersonation> getPagedImpersonationsAsImpersonated(String userId, String realmId, Pageable pageable, LoggedUser loggedUser) throws IllegalArgumentException {
        log.error("Trying to call getPagedImpersonationsAsImpersonated(String userId, String realmId, Pageable pageable, LoggedUser loggedUser) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public Page<Impersonation> getPagedImpersonationsAsImpersonator(String userId, List<String> groupsIds, String realmId, Pageable pageable, LoggedUser loggedUser) throws IllegalArgumentException {
        log.error("Trying to call getPagedImpersonationsAsImpersonator(String userId, List<String> groupsIds, String realmId, Pageable pageable, LoggedUser loggedUser) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public Page<Impersonation> getPagedCurrentImpersonationsAsImpersonator(String userId, List<String> groupsIds, String realmId, Pageable pageable, LoggedUser loggedUser) throws IllegalArgumentException {
        log.error("Trying to call getPagedCurrentImpersonationsAsImpersonator(String userId, List<String> groupsIds, String realmId, Pageable pageable, LoggedUser loggedUser) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public LoggedUser startImpersonation(Impersonation impersonation) throws IllegalArgumentException, AccessDeniedException {
        log.error("Trying to call startImpersonation(Impersonation impersonation) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public LoggedUser startImpersonation(Impersonation impersonation, LoggedUser impersonator) throws IllegalArgumentException, AccessDeniedException {
        log.error("Trying to call startImpersonation(Impersonation impersonation, LoggedUser impersonator) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public LoggedUser endImpersonation() throws IllegalArgumentException {
        log.error("Trying to call endImpersonation() in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }

    @Override
    public LoggedUser endImpersonation(LoggedUser impersonator) throws IllegalArgumentException {
        log.error("Trying to call endImpersonation(LoggedUser impersonator) in community edition.");
        throw new NotImplementedException("Impersonation not implemented in community edition.");
    }
}
