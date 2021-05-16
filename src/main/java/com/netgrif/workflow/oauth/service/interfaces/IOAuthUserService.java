package com.netgrif.workflow.oauth.service.interfaces;

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.oauth.domain.OAuthUser;

public interface IOAuthUserService extends IUserService {

    OAuthUser findByOAuthId(String id);

}
