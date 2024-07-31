package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.Collection;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String>, QuerydslPredicateExecutor<Task>, QuerydslBinderCustomizer<QTask> {

    List<Task> findAllByCaseId(String id);

    Page<Task> findByCaseIdIn(Pageable pageable, Collection<String> ids);

    Page<Task> findByTransitionIdIn(Pageable pageable, Collection<String> ids);

    Page<Task> findByAssigneeId(Pageable pageable, String userId);

    List<Task> findAllByTransitionIdInAndCaseId(Collection<String> transitionIds, String caseId);

    List<Task> findAllByIdIn(Iterable<String> id);

    void deleteAllByProcessId(String processId);

    @Override
    default void customize(QuerydslBindings bindings, QTask qTask) {
    }
}
