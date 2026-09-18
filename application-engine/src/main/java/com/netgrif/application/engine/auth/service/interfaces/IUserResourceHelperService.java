package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.web.responsebodies.UserDto;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;

import java.util.Locale;

public interface IUserResourceHelperService {
    UserResource getResource(LoggedUser loggedUser, Locale locale, boolean small);

    UserDto getLocalisedUser(AbstractUser user, AbstractUser impersonated, Locale locale);

    UserDto getLocalisedUser(AbstractUser user, Locale locale);
}
