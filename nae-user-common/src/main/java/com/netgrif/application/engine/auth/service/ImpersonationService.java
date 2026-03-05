package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.Impersonation;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

public interface ImpersonationService {

    Impersonation getImpersonation(String impersonationId, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException;

    Impersonation createImpersonation(Impersonation impersonation, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException;

    Impersonation updateImpersonation(Impersonation impersonation, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException;

    void removeImpersonations(List<String> impersonationIds, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException;

    void removeImpersonation(String impersonationId, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException;

    void removeImpersonation(Impersonation impersonation, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException;

    Page<Impersonation> getPagedImpersonationsAsImpersonated(String userId, String realmId, Pageable pageable, LoggedUser loggedUser) throws IllegalArgumentException;

    Page<Impersonation> getPagedImpersonationsAsImpersonator(String userId, List<String> groupsIds, String realmId, Pageable pageable, LoggedUser loggedUser) throws IllegalArgumentException;

    Page<Impersonation> getPagedCurrentImpersonationsAsImpersonator(String userId, List<String> groupsIds, String realmId, Pageable pageable, LoggedUser loggedUser) throws IllegalArgumentException;

    LoggedUser startImpersonation(String impersonationId) throws IllegalArgumentException, AccessDeniedException;

    LoggedUser startImpersonation(String impersonationId, LoggedUser loggedUser) throws IllegalArgumentException, AccessDeniedException;

    LoggedUser endImpersonation() throws IllegalArgumentException;

    LoggedUser endImpersonation(LoggedUser impersonator) throws IllegalArgumentException;
}
