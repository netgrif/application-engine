package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.AuthorityEnum;

public interface IBaseAuthorizationService {

    boolean hasAuthority(AuthorityEnum authority);
}
