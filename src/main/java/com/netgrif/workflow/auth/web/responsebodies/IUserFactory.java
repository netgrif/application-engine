package com.netgrif.workflow.auth.web.responsebodies;

import java.util.Locale;

public interface IUserFactory {
    /**
     * @param user the domain User object we want to send to frontend
     * @param locale the locale for translations
     * @return a full version of the user response object, that has all of its attributes set
     */
    User getUser(com.netgrif.workflow.auth.domain.User user, Locale locale);

    /**
     * @param user the domain User object we want to send to frontend
     * @return a small version of the user response object, that has its large attributes (roles, groups, authorities...) cleared
     */
    User getSmallUser(com.netgrif.workflow.auth.domain.User user);
}
