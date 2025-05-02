package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.throwable.InvalidUserTokenException;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.objects.auth.domain.RegisteredUser;

import java.time.LocalDateTime;

public interface UserRegistrationService {

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
