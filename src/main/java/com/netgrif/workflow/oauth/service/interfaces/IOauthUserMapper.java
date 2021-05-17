package com.netgrif.workflow.oauth.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

public interface IOauthUserMapper {

    LoggedUser transform(OAuth2Authentication auth);

}
