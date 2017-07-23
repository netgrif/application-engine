package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.UnactivatedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.RegistrationRequest;

public interface IUnactivatedUserService {

    void removeExpired();

    boolean authorizeToken(String email, String token);

    UnactivatedUser createUnactivatedUser(NewUserRequest request);

    String getEmail(String token);

    User createUser(RegistrationRequest request);

}
