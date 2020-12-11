package com.netgrif.workflow.auth.web.responsebodies;

import java.util.Locale;

public interface IUserFactory {
    User getUser(com.netgrif.workflow.auth.domain.User user, Locale locale, boolean small);
}
