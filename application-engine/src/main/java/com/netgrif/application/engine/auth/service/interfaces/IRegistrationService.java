package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.service.InvalidUserTokenException;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.objects.auth.domain.User;

import java.time.LocalDateTime;

public interface IRegistrationService {

    void removeExpiredUsers();

    void resetExpiredToken();

    void changePassword(User user, String newPassword);

    void encodeUserPassword(User user);

    boolean stringMatchesUserPassword(User user, String passwordToCompare);

    boolean verifyToken(String token);

    User createNewUser(NewUserRequest newUser);

    User registerUser(RegistrationRequest registrationRequest) throws InvalidUserTokenException;

    User resetPassword(String email);

    User recover(String email, String newPassword);

    String generateTokenKey();

    String[] decodeToken(String token) throws InvalidUserTokenException;

    String encodeToken(String email, String tokenKey);

    LocalDateTime generateExpirationDate();

    boolean isPasswordSufficient(String password);
}
