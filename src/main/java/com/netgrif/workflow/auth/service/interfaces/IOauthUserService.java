package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.OauthUser;

public interface IOauthUserService extends IUserService {

    OauthUser saveNewOAuth(OauthUser user);
}
