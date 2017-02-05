package com.fmworkflow.workflow.domain;

import com.fmworkflow.auth.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByCaseId(String id);

    List<Task> findByUser(User user);
}
