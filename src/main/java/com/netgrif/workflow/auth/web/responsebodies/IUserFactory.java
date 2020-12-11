package com.netgrif.workflow.auth.web.responsebodies;

import java.util.Locale;

public interface IUserFactory {
    /**
     * Equivalent to calling getUser(user, locale, false)
     */
    User getUser(com.netgrif.workflow.auth.domain.User user, Locale locale);

    User getUser(com.netgrif.workflow.auth.domain.User user, Locale locale, boolean small);
}
