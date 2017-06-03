package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.Token;

public interface ITokenService {

    boolean authorizeToken(String email, String token);

    void removeExpired();

    Token createToken(String email);

    String getEmail(String token);
}