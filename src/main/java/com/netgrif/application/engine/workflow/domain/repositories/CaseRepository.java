package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaseRepository extends MongoRepository<Case, String>, QuerydslPredicateExecutor<Case>, QuerydslBinderCustomizer<QCase> {

    List<Case> findAllByProcessIdentifier(String identifier);

    List<Case> findAllBy_idIn(Iterable<String> id);

    Page<Case> findAllByUriNodeId(String uri, Pageable pageable);

    List<Case> findAllByPetriNetObjectId(ObjectId petriNetObjectId);

    void deleteAllByPetriNetObjectId(ObjectId petriNetObjectId);

    @Override
    default void customize(QuerydslBindings bindings, QCase qCase) {
    }
}