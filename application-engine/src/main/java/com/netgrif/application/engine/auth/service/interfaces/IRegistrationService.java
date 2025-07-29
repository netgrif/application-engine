package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.service.InvalidUserTokenException;
import com.netgrif.application.engine.auth.web.requestbodies.NewUserRequest;
import com.netgrif.application.engine.auth.web.requestbodies.RegistrationRequest;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;

import java.time.LocalDateTime;

public interface IRegistrationService {

    void removeExpiredUsers();

    void resetExpiredToken();

    void changePassword(AbstractUser user, String newPassword);

    void encodeUserPassword(AbstractUser user);

    boolean stringMatchesUserPassword(AbstractUser user, String passwordToCompare);

    boolean verifyToken(String token);

    AbstractUser createNewUser(NewUserRequest newUser);

    AbstractUser registerUser(RegistrationRequest registrationRequest) throws InvalidUserTokenException;

    AbstractUser resetPassword(String email);

    AbstractUser recover(String email, String newPassword);

    String generateTokenKey();

    String[] decodeToken(String token) throws InvalidUserTokenException;

    String encodeToken(String email, String tokenKey);

    LocalDateTime generateExpirationDate();

    boolean isPasswordSufficient(String password);
}
