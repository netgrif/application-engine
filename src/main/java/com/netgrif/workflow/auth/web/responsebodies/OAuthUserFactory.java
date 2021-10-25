package com.netgrif.workflow.auth.web.responsebodies;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.oauth.domain.OAuthUser;

public class OAuthUserFactory extends UserFactory {

    @Override
    protected User getUser(IUser user) {
        User result;
        if (user instanceof OAuthUser) {
            result = new com.netgrif.workflow.auth.web.responsebodies.OAuthUser((OAuthUser) user);
        } else {
            result = super.getUser(user);
        }
        return result;
    }
}
