package com.netgrif.workflow.oauth.service.interfaces;

import com.netgrif.workflow.auth.domain.LoggedUser;

public interface IOauthUserMapper {

    LoggedUser transform(Object principal);

}
