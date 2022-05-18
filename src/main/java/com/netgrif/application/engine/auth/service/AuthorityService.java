package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.repositories.AuthorityRepository;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
        if (isScope(name)) {
            throw new IllegalArgumentException("The authority name is not valid. Scope is suitable for this function.");
        }
        Authority authority = repository.findByName(name);
        if (authority == null)
            authority = repository.save(new Authority(name));
        return authority;
    }

    @Override
    public Authority save(Authority authority) {
        return repository.save(authority);
    }

    @Override
    public List<Authority> getOrCreate(List<String> authorities) {
        if (authorities == null)
            return Collections.emptyList();
        List<Authority> result = new ArrayList<>();
        authorities.forEach(a -> {
            if (isScope(a)) {
                result.addAll(findByScope(a));
            } else {
                result.add(getOrCreate(a));
            }
        });
        return result;
    }

    @Override
    public Authority delete(String name) {
        if (isScope(name)) {
            throw new IllegalArgumentException("The authority name is not valid. Scope is suitable for this function.");
        }
        Authority authority = repository.findByName(name);
        if (authority == null)
            throw new IllegalArgumentException("Could not find authority with name [" + name + "]");
        repository.delete(authority);
        return authority;
    }

    @Override
    public List<Authority> findByScope(String scope) {
        List<Authority> authorities;
        if (scope.equals("*"))
            authorities = repository.findAll();
        else {
            String prefix = scope.replace("*", "");
            authorities = repository.findAll().stream().filter(authority -> authority.getName().startsWith(prefix)).collect(Collectors.toList());
        }
        return authorities;
    }

    @Override
    public Authority findByName(String name) {
        if (isScope(name)) {
            throw new IllegalArgumentException("The authority name is not valid. Scope is suitable for this function.");
        }
        Authority authority = repository.findByName(name);
        if (authority == null)
            throw new IllegalArgumentException("Could not find authority with name [" + name + "]");
        return authority;
    }

    @Override
    public Optional<Authority> findById(String id) {
        return repository.findById(id);
    }

    public Authority getOne(String id) {
        Optional<Authority> authority = repository.findById(id);
        if (authority.isEmpty())
            throw new IllegalArgumentException("Could not find authority with id [" + id + "]");
        return authority.get();
    }

    private boolean isScope(String authorityName) {
        if (authorityName.endsWith("*"))
            return true;
        else if (authorityName.contains("*"))
            throw new IllegalArgumentException("The authority name or scope is not valid.");
        else
            return false;
    }
}