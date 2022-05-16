package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.Authority;

import java.util.List;
import java.util.Optional;

public interface IAuthorityService {

    List<Authority> findAll();

    List<Authority> findByScope(String scope);

    Authority findByName(String name);

    Optional<Authority> findById(String id);

    Authority getOrCreate(String name);

    Authority save(Authority authority);

    List<Authority> getOrCreate(List<String> authorities);

    Authority delete(String name);

    Authority getOne(String id);
}