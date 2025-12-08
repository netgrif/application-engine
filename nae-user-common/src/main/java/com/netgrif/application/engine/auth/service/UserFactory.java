package com.netgrif.application.engine.auth.service;


import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.dto.response.user.UserDto;

import java.util.Locale;

public interface UserFactory {
    /**
     * @param user   the domain User object we want to send to frontend
     * @param locale the locale for translations
     * @return a full version of the user response object, that has all of its attributes set
     */
    UserDto getUser(AbstractUser user, Locale locale);

    /**
     * @param user         the domain User object we want to send to frontend
     * @param locale       the locale for translations
     * @param impersonated impersonated User object
     * @return a full version of the user response object, that has all of its attributes set
     */
    UserDto getUserWithImpersonation(AbstractUser user, AbstractUser impersonated, Locale locale);
}
