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

    List<Task> findAllByTransitionIdIn(Collection<String> ids);

    Page<Task> findByUserId(Pageable pageable, String userId);

    List<Task> findByUserIdAndFinishDateNotNull(Long userId);

    Task findByTransitionIdAndCaseId(String transitionId, String caseId);

    List<Task> findAllByTransitionIdInAndCaseId(Collection<String> transitionIds, String caseId);

    List<Task> findAllBy_idIn(Iterable<String> id);

    void deleteAllByCaseIdAndUserIdIsNull(String caseId);

    void deleteAllByCaseIdAndFinishDateIsNotNull(String caseId);

    void deleteAllByCaseId(String caseId);

    void deleteAllByProcessId(String processId);

    @Override
    default void customize(QuerydslBindings bindings, QTask qTask) {
        //
    }
}
