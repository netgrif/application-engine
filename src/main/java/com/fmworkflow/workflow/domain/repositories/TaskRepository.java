package com.fmworkflow.workflow.domain.repositories;

import com.fmworkflow.workflow.domain.Task;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    List<Task> findByCaseIdIn(Collection<String> ids);

    List<Task> findByTransitionIdIn(Collection<String> ids);

    List<Task> findByUserId(Long userId);

    List<Task> findByUserIdAndFinishDateNotNull(Long userId);

    List<Task> findAllByAssignRoleInOrDelegateRoleIn(List<String> roles, List<String> roles2);

    void deleteAllByCaseIdAndUserIdIsNull(String caseId);

    void deleteAllByCaseIdAndFinishDateIsNotNull(String caseId);
}
