package com.fmworkflow.auth.service;

public interface ITokenService {
    boolean authorizeToken(String email, String token);
    void removeExpired();
}
