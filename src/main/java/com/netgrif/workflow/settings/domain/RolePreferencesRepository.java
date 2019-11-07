package com.netgrif.workflow.settings.domain;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RolePreferencesRepository extends MongoRepository<RolePreferences, Long> {

    RolePreferences findByProcessRoleId(Long id);
}