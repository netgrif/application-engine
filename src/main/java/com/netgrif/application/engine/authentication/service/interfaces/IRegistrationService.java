package com.netgrif.application.engine.authentication.service.interfaces;

import com.netgrif.application.engine.authentication.domain.Identity;
import com.netgrif.application.engine.authentication.service.InvalidIdentityTokenException;
import com.netgrif.application.engine.authentication.web.requestbodies.NewIdentityRequest;
import com.netgrif.application.engine.authentication.web.requestbodies.RegistrationRequest;

import java.time.LocalDateTime;

public interface IRegistrationService {

    void removeExpiredIdentities();

    void resetExpiredToken();

    void changePassword(Identity identity, String newPassword);

    boolean verifyToken(String token);

    Identity createNewIdentity(NewIdentityRequest newUser);

    Identity registerIdentity(RegistrationRequest registrationRequest) throws InvalidIdentityTokenException;

    boolean matchesIdentityPassword(Identity identity, String passwordToCompare);

    Identity resetPassword(String email);

    Identity recover(String email, String newPassword);

    String generateTokenKey();

    String[] decodeToken(String token) throws InvalidIdentityTokenException;

    String encodeToken(String email, String tokenKey);

    LocalDateTime generateExpirationDate();

    boolean isPasswordSufficient(String password);
}
