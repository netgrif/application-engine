package com.netgrif.application.engine.auth.repository;

import com.netgrif.application.engine.objects.auth.domain.Preferences;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferencesRepository extends MongoRepository<Preferences, Long> {

    Preferences findByUserId(String id);
}