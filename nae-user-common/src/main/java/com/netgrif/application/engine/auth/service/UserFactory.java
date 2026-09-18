package com.netgrif.application.engine.auth.service;


import com.netgrif.application.engine.auth.web.responsebodies.UserDto;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;

import java.util.Locale;

public interface UserFactory {
    /**
     * @param user   the domain User object we want to send to frontend
     * @param locale the locale for translations
     * @return a full version of the user response object, that has all of its attributes set
     */
    UserDto getUser(AbstractUser user, Locale locale);
}
