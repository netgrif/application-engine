package com.netgrif.application.engine.auth.service.interfaces;

public interface IAuthorizationService {
    boolean hasAuthority(String authority);

    boolean hasAnyAuthority(String... authority);
}
