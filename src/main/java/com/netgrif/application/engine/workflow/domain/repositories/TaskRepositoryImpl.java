package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.workflow.domain.QTask;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.core.types.dsl.StringPath;
import org.springframework.data.querydsl.binding.QuerydslBindings;
import org.springframework.data.querydsl.binding.SingleValueBinding;
import org.springframework.stereotype.Repository;

@Repository("taskRepository")
public abstract class TaskRepositoryImpl implements TaskRepository {

    @Override
    public void customize(QuerydslBindings bindings, QTask qTask) {
        bindings.bind(String.class).first((SingleValueBinding<StringPath, String>) StringExpression::equalsIgnoreCase);
    }
}