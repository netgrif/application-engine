package com.netgrif.workflow.oauth.service.interfaces;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.oauth.domain.OAuthUser;
import com.netgrif.workflow.oauth.domain.RemoteUserResource;

public interface IOAuthUserService extends IUserService {

    IUser findByOAuthId(String id);

    IUser findByUsername(String username);

    void loadUser(OAuthUser user, boolean small);

    void loadUser(OAuthUser user, RemoteUserResource resource);

}
