package com.netgrif.application.engine.workflow.domain.repositories;

import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.adapter.spring.workflow.domain.QTask;
import com.netgrif.application.engine.objects.workflow.domain.Task;
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

    Page<Task> findByTransitionIdIn(Pageable pageable, Collection<String> ids);

    List<Task> findAllByTransitionIdIn(Collection<String> ids);

    Page<Task> findByAssignee_Id(Pageable pageable, String userId);

    List<Task> findByAssignee_IdAndFinishDateNotNull(String userId);

    Task findByTransitionIdAndCaseId(String transitionId, String caseId);

    List<Task> findAllByTransitionIdInAndCaseId(Collection<String> transitionIds, String caseId);

    List<Task> findAllBy_idIn(Iterable<ProcessResourceId> id);

    void deleteAllByCaseIdAndAssignee_IdIsNull(String caseId);

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
