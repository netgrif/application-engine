package com.netgrif.application.engine.impersonation.service.interfaces;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.workflow.domain.Case;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

public interface IImpersonationAuthorizationService {

    Page<IUser> getConfiguredImpersonationUsers(String query, LoggedUser impersonator, Pageable pageable);

    boolean canImpersonate(LoggedUser loggedUser, String configId);

    boolean canImpersonateUser(LoggedUser impersonator, String userId);

    Page<Case> searchConfigs(String impersonatorId, Pageable pageable);

    List<Case> searchConfigs(String impersonatorId, String impersonatedId);

    List<Authority> getAuthorities(List<Case> configs, IUser impersonated);

    List<ProcessRole> getRoles(List<Case> configs, IUser impersonated);

    Case getConfig(String configId);

    String getImpersonatedUserId(Case config);

    LocalDateTime getValidUntil(Case config);
}
