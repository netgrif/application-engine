package com.netgrif.application.engine.authentication.service;

import com.netgrif.application.engine.authentication.domain.Authority;
import com.netgrif.application.engine.authentication.domain.repositories.AuthorityRepository;
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AuthorityService implements IAuthorityService {

    private final AuthorityRepository repository;

    public AuthorityService(AuthorityRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Authority> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public Authority getOrCreate(String name) {
        Authority authority = repository.findByName(name);
        if (authority == null) {
            authority = repository.save(new Authority(name));
        }
        return authority;
    }

    @Override
    public Authority getOrCreatePermission(String name) {
        return getOrCreate(Authority.PERMISSION + name);
    }

    @Override
    public Authority getOrCreateRole(String name) {
        return getOrCreate(Authority.ROLE + name);
    }

    @Override
    public List<Authority> getAllPermissions() {
        return repository.findAllByNameStartsWith(Authority.PERMISSION);
    }

    @Override
    public List<Authority> getAllRoles() {
        return repository.findAllByNameStartsWith(Authority.ROLE);
    }

    @Override
    public Authority getOne(String id) {
        Optional<Authority> authority = repository.findById(id);
        if (authority.isEmpty()) {
            throw new IllegalArgumentException("Could not find authority with id [" + id + "]");
        }
        return authority.get();
    }

    @Override
    public List<Authority> findAllByIds(List<String> ids) {
        return repository.findAllByIdIn(ids.stream().map(ObjectId::new).collect(Collectors.toList()));
    }
}