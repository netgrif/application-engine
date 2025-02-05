package com.netgrif.application.engine.petrinet.domain.roles;

import com.netgrif.core.petrinet.domain.roles.ProcessRole;
import com.netgrif.core.workflow.domain.ProcessResourceId;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public interface ProcessRoleRepository extends MongoRepository<com.netgrif.core.petrinet.domain.roles.ProcessRole, String>, QuerydslPredicateExecutor<ProcessRole> {

    Set<com.netgrif.core.petrinet.domain.roles.ProcessRole> findAllByNetId(String netId);

    Set<com.netgrif.core.petrinet.domain.roles.ProcessRole> findAllByImportIdIn(Set<String> importIds);

    Set<com.netgrif.core.petrinet.domain.roles.ProcessRole> findAllByName_DefaultValue(String name);

    Set<com.netgrif.core.petrinet.domain.roles.ProcessRole> findAllByImportId(String importId);

    Set<com.netgrif.core.petrinet.domain.roles.ProcessRole> findAllByGlobalIsTrue();

    @Query("{ '_id.objectId': ?0 }")
    Optional<com.netgrif.core.petrinet.domain.roles.ProcessRole> findByIdObjectId(ObjectId objectId);

    void deleteAllBy_idIn(Collection<ProcessResourceId> ids);


        //TODO: It goes one at a time... make bulk
    default Set<com.netgrif.core.petrinet.domain.roles.ProcessRole> findAllById(Set<String> ids) {
        return ids.stream()
                .map(this::findByCompositeId)
                .flatMap(Optional::stream)
                .collect(Collectors.toSet());
    }

    default Optional<com.netgrif.core.petrinet.domain.roles.ProcessRole> findByCompositeId(String compositeId) {
        String[] parts = compositeId.split(ProcessResourceId.ID_SEPARATOR);
        if (parts.length == 2) {
            String networkId = parts[0];
            ObjectId objectId = new ObjectId(parts[1]);
            return findByNetworkIdAndObjectId(networkId, objectId);
        } else {
            return findByIdObjectId(new ObjectId(compositeId));
        }
    }

    @Query("{ '_id.shortProcessId': ?0, '_id.objectId': ?1 }")
    Optional<com.netgrif.core.petrinet.domain.roles.ProcessRole> findByNetworkIdAndObjectId(String networkId, ObjectId objectId);
}
