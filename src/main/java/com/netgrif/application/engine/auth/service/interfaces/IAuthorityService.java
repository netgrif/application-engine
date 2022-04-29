package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.AuthorityEnum;

import java.util.List;

public interface IAuthorityService {

    List<Authority> findAll();

    Authority getOrCreate(String name);

    Authority getOrCreate(AuthorityEnum authorityEnum);

    List<Authority> getOrCreate(List<AuthorityEnum> authorities);

    Authority getOne(String id);
}