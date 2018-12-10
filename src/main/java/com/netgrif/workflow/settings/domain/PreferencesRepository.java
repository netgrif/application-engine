package com.netgrif.workflow.settings.domain;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PreferencesRepository extends MongoRepository<Preferences, ObjectId> {

    Preferences findByUserId(Long id);
}