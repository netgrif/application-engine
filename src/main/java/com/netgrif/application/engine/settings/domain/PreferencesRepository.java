package com.netgrif.application.engine.settings.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PreferencesRepository extends MongoRepository<Preferences, Long> {

    Optional<Preferences> findByIdentityId(String id);
}