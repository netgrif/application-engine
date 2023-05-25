package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.domain.AuthorityProperties;
import com.netgrif.application.engine.auth.domain.repositories.AuthorityRepository;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.rest.webmvc.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

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
     * Property class for authorities
     * */
    @Autowired
    private AuthorityProperties authorityProperties;

    private static final String SCOPE_SUFFIX = "*";

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
            throw new IllegalArgumentException("The authority name is not valid. Scope is not suitable for this function.");
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
            throw new ResourceNotFoundException("Could not find authority with name [" + name + "]");
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
        if (scope.equals(SCOPE_SUFFIX))
            authorities = repository.findAll();
        else if (isScope(scope)) {
            String prefix = scope.replace(SCOPE_SUFFIX, Strings.EMPTY);
            authorities = repository.findAllByNameStartsWith(prefix);
        } else {
            authorities = Collections.singletonList(repository.findByName(scope));
        }
        return authorities;
    }

    /**
     * Returns authority based on name, throws exception if authority name is not valid or authority with provided name
     * cannot be found.
     * @param name of authority
     * @return authority object
     * */
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
    public Optional<Authority> findOptionalByName(String id) {
        return Optional.of(repository.findByName(id));
    }

    /**
     * Returns the default authorities for simple user
     * @return list of authorities
     * */
    @Override
    @Cacheable("defaultUserAuthoritiesCache")
    public Set<Authority> getDefaultUserAuthorities() {
        return authorityProperties.getDefaultUserAuthorities().stream().map(this::findByScope).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Returns the default authorities for anonymous user
     * @return list of authorities
     * */
    @Override
    @Cacheable("defaultAnonymousAuthoritiesCache")
    public Set<Authority> getDefaultAnonymousAuthorities() {
        return authorityProperties.getDefaultAnonymousAuthorities().stream().map(this::findByScope).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Returns the default authorities for admin user
     * @return list of authorities
     * */
    @Override
    @Cacheable("defaultAdminAuthoritiesCache")
    public Set<Authority> getDefaultAdminAuthorities() {
        return authorityProperties.getDefaultAdminAuthorities().stream().map(this::findByScope).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    /**
     * Checks for authorityName, if it is valid scope name
     * @param authorityName of authority
     * @return boolean whether the provided name is valid scope name
     * */
    private boolean isScope(String authorityName) {
        if (authorityName.contains(SCOPE_SUFFIX) && authorityName.indexOf(SCOPE_SUFFIX) != authorityName.length() - 1)
            throw new IllegalArgumentException("The authority name or scope is not valid.");
        else return authorityName.endsWith(SCOPE_SUFFIX);
    }
}