package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.AuthorityEnum;
import com.netgrif.application.engine.auth.domain.repositories.AuthorityRepository;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthorityService implements IAuthorityService {

    @Autowired
    private AuthorityRepository repository;

    @Override
    public List<Authority> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public Authority getOrCreate(String name) {
        Authority authority = repository.findByName(name);
        if (authority == null)
            authority = repository.save(new Authority(name));
        return authority;
    }

    @Override
    public Authority getOrCreate(AuthorityEnum authorityEnum) {
        return getOrCreate(authorityEnum.name());
    }

    @Override
    public List<Authority> getOrCreate(List<AuthorityEnum> authorities) {
        if (authorities == null)
            return Collections.emptyList();
        return authorities.stream().map(this::getOrCreate).collect(Collectors.toList());
    }

    @Override
    public Authority delete(String name) {
        Authority authority = repository.findByName(name);
        if (authority == null)
            throw new IllegalArgumentException("Could not find authority with name [" + name + "]");
        repository.delete(authority);
        return authority;
    }

    public Authority getOne(String id) {
        Optional<Authority> authority = repository.findById(id);
        if (authority.isEmpty())
            throw new IllegalArgumentException("Could not find authority with id [" + id + "]");
        return authority.get();
    }
}