package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.Group;
import com.netgrif.application.engine.workflow.service.interfaces.ICrudSystemCaseService;

import java.util.Optional;

public interface IGroupService extends ICrudSystemCaseService<Group> {
    Optional<Group> findByName(String name);
    boolean existsByName(String name);
}
