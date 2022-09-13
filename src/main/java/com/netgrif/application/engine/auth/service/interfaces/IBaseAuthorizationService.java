package com.netgrif.application.engine.auth.service.interfaces;

public interface IBaseAuthorizationService {

    boolean hasAnyAuthority(String[] authorizingObject);
}
