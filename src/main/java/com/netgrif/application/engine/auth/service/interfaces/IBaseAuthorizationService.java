package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.AuthorizingObject;

public interface IBaseAuthorizationService {

    boolean hasAuthority(String authorityName);
}
