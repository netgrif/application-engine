package com.netgrif.application.engine.petrinet.domain.roles;

import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.stereotype.Repository;
import org.springframework.data.util.Pair;

import java.util.*;
import java.util.stream.Collectors;

@Repository
public interface ProcessRoleRepository extends MongoRepository<ProcessRole, String>, QuerydslPredicateExecutor<ProcessRole> {

    Optional<ProcessRole> findByImportId(String importId);

    Page<ProcessRole> findAllByProcessId(String netId, Pageable pageable);

    List<ProcessRole> findAllByImportIdIn(Set<String> importIds);

    Page<ProcessRole> findAllByName_DefaultValue(String name, Pageable pageable);

    Page<ProcessRole> findAllByImportId(String importId, Pageable pageable);

    Page<ProcessRole> findAllByGlobalIsTrue(Pageable pageable);

    @Query("{ '_id.objectId': ?0 }")
    Optional<ProcessRole> findByIdObjectId(ObjectId objectId);

    @Query("{ '_id.objectId': { $in: ?0 } }")
    List<ProcessRole> findByObjectIds(Collection<ObjectId> objectIds);

    void deleteAllBy_idIn(Collection<ProcessResourceId> ids);

    default Set<ProcessRole> findAllByIdsSet(Collection<String> ids) {
        Map<Boolean, List<String>> partitionedIds = ids.stream()
                .collect(Collectors.partitioningBy(id -> id.contains(ProcessResourceId.ID_SEPARATOR)));

        List<ObjectId> forObjectIds = partitionedIds.get(false).stream()
                .map(ObjectId::new)
                .collect(Collectors.toList());

        List<ObjectId> forNetworkObjectIds = new ArrayList<>();
        List<String> forNetworkNetworkIds = new ArrayList<>();
        partitionedIds.get(true).forEach(id -> {
                    String[] parts = id.split(ProcessResourceId.ID_SEPARATOR);
                    forNetworkNetworkIds.add(parts[0]);
                    forNetworkObjectIds.add(new ObjectId(parts[1]));
                });

        List<ProcessRole> processRoles = new ArrayList<>();
        processRoles.addAll(findByObjectIds(forObjectIds));
        processRoles.addAll(findByNetworkIdsAndObjectIds(forNetworkNetworkIds, forNetworkObjectIds));
        return new HashSet<>(processRoles);
    }

    default Optional<ProcessRole> findByCompositeId(String compositeId) {
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
    Optional<ProcessRole> findByNetworkIdAndObjectId(String networkId, ObjectId objectId);

    @Query("{ $or: [ { '_id.shortProcessId': ?0, '_id.objectId': ?1 } ] }")
    List<ProcessRole> findByNetworkIdsAndObjectIds(Collection<String> networkIds, Collection<ObjectId> objectIds);
}