package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.adapter.spring.workflow.domain.QTask;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.querydsl.core.types.Predicate;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.querydsl.binding.QuerydslBinderCustomizer;
import org.springframework.data.querydsl.binding.QuerydslBindings;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends MongoRepository<Task, String>, QuerydslPredicateExecutor<Task>, QuerydslBinderCustomizer<QTask> {

    List<Task> findAllByCaseId(String id);

    Page<Task> findByCaseIdIn(Pageable pageable, Collection<String> ids);

    Page<Task> findByTransitionIdInAndWorkspaceId(Pageable pageable, Collection<String> ids, String workspaceId);

    List<Task> findAllByTransitionIdInAndWorkspaceId(Collection<String> ids, String workspaceId);

    Page<Task> findByUserId(Pageable pageable, String userId);

    List<Task> findByUserIdAndWorkspaceIdAndFinishDateNotNull(Long userId, String workspaceId);

    Task findByTransitionIdAndCaseId(String transitionId, String caseId);

    List<Task> findAllByTransitionIdInAndCaseId(Collection<String> transitionIds, String caseId);

    List<Task> findAllBy_idIn(Iterable<ProcessResourceId> id);

    void deleteAllByCaseIdAndUserIdIsNull(String caseId);

    void deleteAllByCaseIdAndFinishDateIsNotNull(String caseId);

    void deleteAllByCaseId(String caseId);

    void deleteAllByProcessId(String processId);

    @Query("{ '_id.objectId': ?0 }")
    Optional<Task> findByIdObjectId(ObjectId objectId);

    @Override
    default void customize(QuerydslBindings bindings, QTask qTask) {
        //
    }
}
