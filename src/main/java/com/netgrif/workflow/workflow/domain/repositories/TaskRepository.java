package com.netgrif.workflow.workflow.domain.repositories;

import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;

public interface TaskRepository extends MongoRepository<Task, String> {
    Page<Task> findByCaseIdIn(Pageable pageable, Collection<String> ids);

    Page<Task> findByTransitionIdIn(Pageable pageable, Collection<String> ids);

    Page<Task> findByUserId(Pageable pageable, Long userId);

    List<Task> findByUserIdAndFinishDateNotNull(Long userId);

    Page<Task> findAllByAssignRoleInOrDelegateRoleIn(Pageable pageable, List<String> roles, List<String> roles2);

    void deleteAllByCaseIdAndUserIdIsNull(String caseId);

    void deleteAllByCaseIdAndFinishDateIsNotNull(String caseId);
}
