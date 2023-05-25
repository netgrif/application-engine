package com.netgrif.application.engine.auth.service.interfaces;

import com.netgrif.application.engine.auth.domain.Authority;
import org.springframework.cache.annotation.Cacheable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Interface for managing authorities in NAE
 * */
public interface IAuthorityService {

    /**
     * Finds all authorities in the system
     * @return list of all authorities
     * */
    List<Authority> findAll();

    /**
     * Finds all authorities of given scope
     * @param scope of authorities
     * @return list of authorities of given scopr
     * */
    List<Authority> findByScope(String scope);

    /**
     * Finds authority of given name
     * @param name of authority to be searched
     * @return authority object
     * */
    Authority findByName(String name);

    /**
     * Finds authority by ID
     * @param id of authority
     * @return optional of authority object
     * */
    Optional<Authority> findOptionalByName(String name);

    /**
     * Returns or creates authority of given name if it does not exist.
     * @param name of the authority to be retrieved
     * @return authority objects
     * */
    Authority getOrCreate(String name);

    /**
     * Saves authority object and retrieves it
     * @param authority to be saved
     * @return saved authority object
     * */
    Authority save(Authority authority);

    /**
     * Returns or creates authorities based on provided list of names
     * @param authorities list of authority names
     * @return list of authority objects
     * */
    List<Authority> getOrCreate(List<String> authorities);

    /**
     * Removes authority object based on name
     * @param name of authority to be removed
     * */
    void delete(String name);

    Set<Authority> getDefaultUserAuthorities();

    Set<Authority> getDefaultAnonymousAuthorities();

    Set<Authority> getDefaultAdminAuthorities();
}