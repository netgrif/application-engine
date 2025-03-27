package com.netgrif.application.engine.authentication.service;

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
    public List<SessionRole> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional
    public SessionRole getOrCreate(String name) {
        SessionRole sessionRole = repository.findByName(name);
        if (sessionRole == null) {
            sessionRole = repository.save(new SessionRole(name));
        }
        return sessionRole;
    }

    @Override
    public SessionRole getOrCreatePermission(String name) {
        return getOrCreate(SessionRole.PERMISSION + name);
    }

    @Override
    public SessionRole getOrCreateRole(String name) {
        return getOrCreate(SessionRole.ROLE + name);
    }

    @Override
    public List<SessionRole> getAllPermissions() {
        return repository.findAllByNameStartsWith(SessionRole.PERMISSION);
    }

    @Override
    public List<SessionRole> getAllRoles() {
        return repository.findAllByNameStartsWith(SessionRole.ROLE);
    }

    @Override
    public SessionRole getOne(String id) {
        Optional<SessionRole> authority = repository.findById(id);
        if (authority.isEmpty()) {
            throw new IllegalArgumentException("Could not find authority with id [" + id + "]");
        }
        return authority.get();
    }

    @Override
    public List<SessionRole> findAllByIds(List<String> ids) {
        return repository.findAllByIdIn(ids.stream().map(ObjectId::new).collect(Collectors.toList()));
    }
}