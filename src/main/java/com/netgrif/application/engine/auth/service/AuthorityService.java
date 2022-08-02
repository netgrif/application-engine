package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.repositories.AuthorityRepository;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Service to manage authorities in NAE
 * */
@Service
public class AuthorityService implements IAuthorityService {

    /**
     * Repository to communicate with database
     * */
    @Autowired
    private AuthorityRepository repository;

    /**
     * Retrieve all authorities from database
     * */
    @Override
    public List<Authority> findAll() {
        return repository.findAll();
    }

    /**
     * Returns or creates authority based on provided name from/into database
     * @param name of searched authority
     * @return Authority object
     * */
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

    /**
     * Saves new or updated to database
     * @param authority that will be saved
     * @return saved authority object
     * */
    @Override
    public Authority save(Authority authority) {
        return repository.save(authority);
    }

    /**
     * Returns or creates authorities based on provided names from/into database
     * @param authorities of searched authority
     * @return list of authority objects
     * */
    @Override
    @Transactional
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

    /**
     * Removes authority from database based on provided name if exists
     * @param name of authority to be deleted
     * */
    @Override
    public void delete(String name) {
        if (isScope(name)) {
            throw new IllegalArgumentException("The authority name is not valid. Scope is suitable for this function.");
        }
        Authority authority = repository.findByName(name);
        if (authority == null)
            throw new IllegalArgumentException("Could not find authority with name [" + name + "]");
        repository.delete(authority);
    }

    /**
     * Returns authorities of provided scope. A scope contains authorities of the same name prefix, such as authorities
     * of PROCESS scope: PROCESS_UPLOAD, PROCESS_DELETE etc.
     * @param scope to be searched for
     * @return list of authorities of given scope
     * */
    @Override
    public List<Authority> findByScope(String scope) {
        List<Authority> authorities;
        if (scope.equals("*"))
            authorities = repository.findAll();
        else {
            String prefix = scope.replace("*", "");
            authorities = repository.findAllByNameStartsWith(prefix);
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

    /**
     * Returns authority from database based on provided ID
     * @param id of authority to be retrieved
     * @return optional of authority
     * */
    @Override
    public Optional<Authority> findById(String id) {
        return Optional.of(repository.findBy_id(new ObjectId(id)));
    }

    /**
     * Returns authority from database based on provided ID if exists
     * @param id of authority to be retrieved
     * @return authority object
     * */
    @Override
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