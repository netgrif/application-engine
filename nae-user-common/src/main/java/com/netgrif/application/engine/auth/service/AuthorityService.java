package com.netgrif.application.engine.auth.service;

import com.netgrif.application.engine.objects.auth.domain.Authority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Collection;

/**
 * Service interface for managing {@link Authority} entities.
 */
public interface AuthorityService {

    /**
     * Retrieves all {@link Authority} entities with pagination support.
     *
     * @param pageable the {@link Pageable} object containing pagination information.
     * @return a {@link Page} of {@link Authority} entities.
     */
    Page<Authority> findAll(Pageable pageable);

    /**
     * Retrieves an existing {@link Authority} entity by its name or creates a new one if it does not exist.
     *
     * @param name the name of the {@link Authority}.
     * @return the {@link Authority} entity.
     */
    Authority getOrCreate(String name);

    /**
     * Retrieves a single {@link Authority} entity by its unique identifier.
     *
     * @param id the unique identifier of the {@link Authority}.
     * @return the {@link Authority} entity.
     */
    Authority getOne(String id);

    /**
     * Retrieves all {@link Authority} entities matching the given list of identifiers, with pagination support.
     *
     * @param ids      the list of unique identifiers for the {@link Authority} entities.
     * @param pageable the {@link Pageable} object containing pagination information.
     * @return a {@link Page} of {@link Authority} entities.
     */
    Page<Authority> findAllByIds(Collection<String> ids, Pageable pageable);
}
