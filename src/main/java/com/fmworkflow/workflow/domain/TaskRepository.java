package com.fmworkflow.workflow.domain;

import com.fmworkflow.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCaseIdIn(Collection<String> ids);

    List<Task> findByTransitionIdIn(Collection<String> ids);

    List<Task> findByUser(User user);

    List<Task> findByUserAndFinishDateNotNull(User user);

    List<Task> findAllByAssignRoleInOrDelegateRoleIn(List<String> roles, List<String> roles2);

    void deleteAllByCaseIdAndUserIsNull(String caseId);

    void deleteAllByCaseIdAndFinishDateIsNotNull(String caseId);
}
