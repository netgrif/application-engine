package com.netgrif.workflow.workflow.domain.repositories;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.QCase;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.List;

public interface CaseRepository extends MongoRepository<Case, String>, QueryDslPredicateExecutor<Case>, QuerydslBinderCustomizer<QCase> {

    Page<Case> findAllByAuthor(Long authorId, Pageable pageable);

    List<Case> findAllByPetriNetId(String id);

    @Override
    default void customize(QuerydslBindings bindings, QCase qCase) {
    }
}