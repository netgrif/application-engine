package com.netgrif.workflow.workflow.domain.repositories;

import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.QCase;
import com.netgrif.workflow.workflow.domain.QDataField;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;

import java.util.List;

public interface CaseRepository extends MongoRepository<Case, String>, QueryDslPredicateExecutor<Case>, QuerydslBinderCustomizer<QCase> {

    Page<Case> findAllByAuthor(Long authorId, Pageable pageable);

    List<Case> findAllByPetriNetId(String id);

    @Override
    default void customize(QuerydslBindings bindings, QCase qCase) {
        bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::equalsIgnoreCase);
        bindings.bind(qCase.dataSet).first((path, map) ->
                map.entrySet().stream()
                        .map(o -> {
                            QDataField field = qCase.dataSet.get(o.getKey());
                            if (field == null || o.getValue() == null || o.getValue().getValue() == null)
                                return Expressions.asBoolean(false);
                            return field.value.eq(o.getValue().getValue().toString());
                        })
                        .reduce(BooleanExpression::and).get());
    }
}