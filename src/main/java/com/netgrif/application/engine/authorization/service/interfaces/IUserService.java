package com.netgrif.application.engine.authorization.service.interfaces;

import com.netgrif.application.engine.authorization.domain.User;
import com.netgrif.application.engine.workflow.service.interfaces.ICrudSystemCaseService;

import java.util.List;
import java.util.Optional;

public interface IUserService extends ICrudSystemCaseService<User> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    List<User> findAll();
}
