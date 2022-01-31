package com.netgrif.application.engine.settings.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferencesRepository extends MongoRepository<Preferences, Long> {

    Preferences findByUserId(String id);
}