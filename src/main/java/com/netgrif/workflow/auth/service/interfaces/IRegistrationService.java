package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.service.InvalidUserTokenException;
import com.netgrif.workflow.auth.web.requestbodies.NewUserRequest;
import com.netgrif.workflow.auth.web.requestbodies.RegistrationRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public interface IRegistrationService {

    void removeExpiredUsers();

    void resetExpiredToken();

    void changePassword(User user, String newPassword);

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
