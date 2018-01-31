package com.netgrif.workflow.auth.service.interfaces;

import com.netgrif.workflow.auth.domain.UserProcessRole;

import java.util.List;

public interface IUserProcessRoleService {

    List<UserProcessRole> findAllMinusDefault();

    UserProcessRole findDefault();
}