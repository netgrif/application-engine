package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.RegisteredUser;
import com.netgrif.application.engine.auth.service.InvalidUserTokenException;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

public interface IRegistrationService {

    void removeExpiredUsers();

    void resetExpiredToken();

    void changePassword(RegisteredUser user, String newPassword);

    void encodeUserPassword(RegisteredUser user);

    boolean stringMatchesUserPassword(RegisteredUser user, String passwordToCompare);

    boolean verifyToken(String token);

    RegisteredUser createNewUser(NewUserRequest newUser);

    RegisteredUser registerUser(RegistrationRequest registrationRequest) throws InvalidUserTokenException;

    RegisteredUser resetPassword(String email);

    RegisteredUser recover(String email, String newPassword);

    String generateTokenKey();

    String[] decodeToken(String token) throws InvalidUserTokenException;

    String encodeToken(String email, String tokenKey);

    LocalDateTime generateExpirationDate();

    boolean isPasswordSufficient(String password);
}
