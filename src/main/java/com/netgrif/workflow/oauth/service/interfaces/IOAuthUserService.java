package com.netgrif.workflow.oauth.service.interfaces;

import com.netgrif.workflow.auth.domain.IUser;
import com.netgrif.workflow.auth.service.interfaces.IUserService;

public interface IOAuthUserService extends IUserService {

    IUser findByOAuthId(String id);

    IUser findByUsername(String username);
}
