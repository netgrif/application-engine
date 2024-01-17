package com.netgrif.application.engine.integration.plugins.repository;

import com.netgrif.application.engine.integration.plugins.domain.Plugin;
import org.bson.types.ObjectId;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(
        value = "nae.plugin.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public interface PluginRepository extends MongoRepository<Plugin, ObjectId> {
    Plugin findByIdentifier(String identifier);
}
