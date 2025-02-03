package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.adapter.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.domain.ProcessResourceId;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaseRepository extends MongoRepository<Case, String>, QuerydslPredicateExecutor<Case>, QuerydslBinderCustomizer<QCase> {

    List<Case> findAllByProcessIdentifier(String identifier);

    List<Case> findAllBy_idIn(Iterable<String> id);

    @Query("{ '_id.objectId': { $in: ?0 } }")
    List<Case> findAllByObjectIdsIn(List<ObjectId> objectIds);

    Page<Case> findAllByUriNodeId(String uri, Pageable pageable);

    List<Case> findAllByPetriNetObjectId(ObjectId petriNetObjectId);

    void deleteAllByPetriNetObjectId(ObjectId petriNetObjectId);

    @Query("{ '_id.objectId': ?0 }")
    Optional<Case> findByIdObjectId(ObjectId objectId);

    default Optional<Case> findById(String compositeId) {
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
    Optional<Case> findByNetworkIdAndObjectId(String ProcessId, ObjectId objectId);

    @Override
    default void customize(QuerydslBindings bindings, QCase qCase) {
    }
}
