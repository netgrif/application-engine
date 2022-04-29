package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.repositories.AuthorityRepository;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

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

    public Authority getOne(String id) {
        Optional<Authority> authority = repository.findById(id);
        if (!authority.isPresent())
            throw new IllegalArgumentException("Could not find authority with id [" + id + "]");
        return authority.get();
    }
}