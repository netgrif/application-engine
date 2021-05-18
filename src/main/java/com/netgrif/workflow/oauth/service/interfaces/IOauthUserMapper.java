package com.netgrif.workflow.oauth.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import org.springframework.security.core.Authentication;

public interface IOauthUserMapper {

    LoggedUser transform(Authentication principal);

}
