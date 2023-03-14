package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserResourceHelperService;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.auth.web.responsebodies.IUserFactory;
import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
public class UserResourceHelperService implements IUserResourceHelperService {

    private final IUserService userService;
    private final IUserFactory userFactory;
    private final IImpersonationService impersonationService;

    public UserResourceHelperService(IUserService userService, IUserFactory userFactory, IImpersonationService impersonationService) {
        this.userService = userService;
        this.userFactory = userFactory;
        this.impersonationService = impersonationService;
    }

    @Override
    public UserResource getResource(LoggedUser loggedUser, Locale locale, boolean small) {
        IUser user = userService.findById(loggedUser.getId(), small);
        User result = loggedUser.isImpersonating() ?
                getLocalisedUser(user, getImpersonated(loggedUser, small), locale) :
                getLocalisedUser(user, locale);
        return new UserResource(result, "profile");
    }

    @Override
    public User getLocalisedUser(IUser user, IUser impersonated, Locale locale) {
        User localisedUser = getLocalisedUser(user, locale);
        User impersonatedUser = userFactory.getUser(impersonated, locale);
        localisedUser.setImpersonated(impersonatedUser);
        return localisedUser;
    }

    @Override
    public User getLocalisedUser(IUser user, Locale locale) {
        return userFactory.getUser(user, locale);
    }

    protected IUser getImpersonated(LoggedUser loggedUser, boolean small) {
        IUser impersonated = userService.findById(loggedUser.getImpersonated().getId(), small);
        return impersonationService.reloadImpersonatedUserRoles(impersonated, loggedUser.getId());
    }
}
