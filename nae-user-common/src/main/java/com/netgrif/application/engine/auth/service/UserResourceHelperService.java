package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;

import java.util.Locale;

public interface UserResourceHelperService {
    UserResource getResource(LoggedUser loggedUser, Locale locale, boolean small);

    User getLocalisedUser(IUser user, IUser impersonated, Locale locale);

    User getLocalisedUser(IUser user, Locale locale);
}
