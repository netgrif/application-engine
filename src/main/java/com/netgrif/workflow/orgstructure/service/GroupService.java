package com.netgrif.workflow.orgstructure.service;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.orgstructure.domain.GroupRepository;
import lombok.extern.java.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@Log
public class GroupService implements IGroupService {

    @Autowired
    private GroupRepository repository;

    @Override
    public Group save(Group group) {
        log.info("Saving group " + group.getName());
        return repository.save(group);
    }

    @Override
    public Set<Group> findAll() {
        return new HashSet<>((Collection<Group>) repository.findAll());
    }

    @Override
    public Set<Group> findAllById(Collection<Long> groupIds) {
        return new HashSet<>((Collection<Group>) repository.findAllById(groupIds));
    }

    @Override
    public void delete(Group group) {
        repository.delete(group);
    }
}