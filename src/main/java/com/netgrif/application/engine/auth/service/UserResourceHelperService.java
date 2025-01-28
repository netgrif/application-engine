package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.auth.web.responsebodies.IProcessRoleFactory;
import com.netgrif.application.engine.auth.web.responsebodies.ProcessRole;
import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import com.netgrif.application.engine.petrinet.service.interfaces.IProcessRoleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserResourceHelperService {

    private final IUserService userService;
    private final IImpersonationService impersonationService;
    private final IProcessRoleService processRoleService;
    private final IProcessRoleFactory processRoleFactory;

    public UserResourceHelperService(IUserService userService, IImpersonationService impersonationService, IProcessRoleService processRoleService, IProcessRoleFactory processRoleFactory) {
        this.userService = userService;
        this.impersonationService = impersonationService;
        this.processRoleService = processRoleService;
        this.processRoleFactory = processRoleFactory;
    }

    public UserResource getResource(LoggedUser loggedUser, Locale locale) {
        IUser user = userService.findById(loggedUser.getId());
        User result = loggedUser.isImpersonating() ?
                getLocalisedUser(user, getImpersonated(loggedUser), locale) :
                getLocalisedUser(user, locale);
        return new UserResource(result, "profile");
    }

    public User getLocalisedUser(IUser user, IUser impersonated, Locale locale) {
        User localisedUser = getLocalisedUser(user, locale);
        User impersonatedUser = getUser(impersonated, locale);
        localisedUser.setImpersonated(impersonatedUser);
        return localisedUser;
    }

    public User getLocalisedUser(IUser user, Locale locale) {
        return getUser(user, locale);
    }

    protected IUser getImpersonated(LoggedUser loggedUser) {
        IUser impersonated = userService.findById(loggedUser.getImpersonated().getId());
        return impersonationService.reloadImpersonatedUserRoles(impersonated, loggedUser.getId());
    }

    public User getUser(IUser user, Locale locale) {
        User result = User.createUser(user);

        String defaultRoleId = processRoleService.defaultRole().getStringId();
        String anonymousRoleId = processRoleService.anonymousRole().getStringId();
        result.setProcessRoles(user.getProcessRoles().stream().map(processRole -> {
            if (processRole.getStringId().equals(defaultRoleId)) {
                return new ProcessRole(processRole, locale);
            }
            if (processRole.getStringId().equals(anonymousRoleId)) {
                return new ProcessRole(processRole, locale);
            }
            return processRoleFactory.getProcessRole(processRole, locale);
        }).collect(Collectors.toSet()));

        return result;
    }
}
