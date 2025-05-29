package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.ApplicationRoleAssignment;

import java.util.List;

public interface ApplicationRoleAssignmentRepository {
    List<ApplicationRoleAssignment> findAllByActor(String actorId);
}
