package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.web.responsebodies.UserDto;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserResourceHelperService;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;
import com.netgrif.application.engine.impersonation.service.interfaces.IImpersonationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Slf4j
@Service
public class UserResourceHelperService implements IUserResourceHelperService {

    @Autowired
    private UserService userService;

    @Autowired
    private UserFactory userFactory;

    @Autowired
    private IImpersonationService impersonationService;

    @Override
    public UserResource getResource(LoggedUser loggedUser, Locale locale, boolean small) {
        AbstractUser user = userService.findById(loggedUser.getStringId(), null);
        // TODO: impersonation
//        User result = loggedUser.isImpersonating() ?
//                getLocalisedUser(user, getImpersonated(loggedUser, small), locale) :
//                getLocalisedUser(user, locale);
        UserDto result = getLocalisedUser(user, locale);
        return new UserResource(result, "profile");
    }

    @Override
    public UserDto getLocalisedUser(AbstractUser user, AbstractUser impersonated, Locale locale) {
        UserDto localisedUser = getLocalisedUser(user, locale);
        UserDto impersonatedUser = userFactory.getUser(impersonated, locale);
        localisedUser.setImpersonated(impersonatedUser);
        return localisedUser;
    }

    @Override
    public UserDto getLocalisedUser(AbstractUser user, Locale locale) {
        return userFactory.getUser(user, locale);
    }

    // TODO: for impersonation
//    protected AbstractUser getImpersonated(LoggedUser loggedUser, boolean small) {
//        AbstractUser impersonated = userService.findById(loggedUser.getImpersonated().getId(), null);
//        return impersonationService.reloadImpersonatedUserRoles(impersonated, loggedUser.getId());
//    }
}
