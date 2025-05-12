package com.netgrif.application.engine.impersonation.service.interfaces;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.Authority;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface IImpersonationAuthorizationService {

    Page<AbstractUser> getConfiguredImpersonationUsers(String query, LoggedUser impersonator, Pageable pageable);

    boolean canImpersonate(LoggedUser loggedUser, String configId);

    boolean canImpersonateUser(LoggedUser impersonator, String userId);

    Page<Case> searchConfigs(String impersonatorId, Pageable pageable);

    List<Case> searchConfigs(String impersonatorId, String impersonatedId);

    List<Authority> getAuthorities(Collection<Case> configs, AbstractUser impersonated);

    List<ProcessRole> getRoles(Collection<Case> configs, AbstractUser impersonated);

    Case getConfig(String configId);

    String getImpersonatedUserId(Case config);

    LocalDateTime getValidUntil(Case config);
}
