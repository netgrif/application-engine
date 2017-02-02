package com.fmworkflow.auth.service;

import com.fmworkflow.auth.domain.Token;

public interface ITokenService {
    boolean authorizeToken(String email, String token);
    void removeExpired();

    Token createToken(String email);
}
