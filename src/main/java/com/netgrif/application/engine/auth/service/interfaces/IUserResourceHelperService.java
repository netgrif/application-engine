package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.auth.web.responsebodies.UserResource;

import java.util.Locale;

public interface IUserResourceHelperService {
    UserResource getResource(LoggedUser loggedUser, Locale locale, boolean small);

    User getLocalisedUser(IUser user, IUser impersonated, Locale locale);

    User getLocalisedUser(IUser user, Locale locale);
}
