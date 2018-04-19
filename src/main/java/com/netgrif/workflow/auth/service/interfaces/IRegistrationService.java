package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.RegistrationRequest;
import org.springframework.stereotype.Service;

@Service
public interface IRegistrationService {

    void removeExpiredUsers();

    boolean verifyToken(String email, String token);

    String getEmailToToken(String token);

    User createNewUser(NewUserRequest newUser);

    User registerUser(RegistrationRequest registrationRequest);
}
