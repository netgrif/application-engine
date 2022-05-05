package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.AuthorizingObject;

import java.util.List;

public interface IAuthorityService {

    List<Authority> findAll();

    Authority getOrCreate(String name);

    Authority getOrCreate(AuthorizingObject authorizingObject);

    Authority save(Authority authority);

    List<Authority> getOrCreate(List<AuthorizingObject> authorities);

    Authority delete(String name);

    Authority getOne(String id);
}