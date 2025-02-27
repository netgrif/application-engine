package com.netgrif.application.engine.authentication.service.interfaces;

public interface IAuthorizationService {
    boolean hasAuthority(String authority);
}
