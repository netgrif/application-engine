package com.netgrif.application.engine.auth.service;


import com.netgrif.application.engine.auth.web.responsebodies.User;
import com.netgrif.application.engine.objects.auth.domain.IUser;
import org.springframework.security.core.Authentication;

import java.util.Locale;

public interface UserFactory {
    /**
     * @param user   the domain User object we want to send to frontend
     * @param locale the locale for translations
     * @return a full version of the user response object, that has all of its attributes set
     */
    User getUser(IUser user, Locale locale);

    /**
     * @param user the domain User object we want to send to frontend
     * @return a small version of the user response object, that has its large attributes (roles, groups, authorities...) cleared
     */
    User getSmallUser(IUser user);
}
