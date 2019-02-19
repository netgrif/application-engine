package com.netgrif.workflow.orgstructure.service;

import com.netgrif.workflow.orgstructure.domain.Group;

import java.util.Collection;
import java.util.Set;

public interface IGroupService {

    Group save(Group group);

    Set<Group> findAll();

    Set<Group> findAllById(Collection<Long> groupIds);

    void delete(Group group);
}