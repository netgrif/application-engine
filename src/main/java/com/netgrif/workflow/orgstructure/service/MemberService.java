package com.netgrif.workflow.orgstructure.service;

import com.netgrif.workflow.orgstructure.domain.Group;
import com.netgrif.workflow.orgstructure.domain.Member;
import com.netgrif.workflow.orgstructure.domain.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Set;

@Service
public class MemberService implements IMemberService {

    @Autowired
    private MemberRepository repository;

    @Autowired
    private IGroupService groupService;

    @Override
    public Member save(Member member) {
        return repository.save(member);
    }

    @Override
    public Set<Member> findAllByGroups(Collection<Long> groupIds) {
        Set<Group> groups = groupService.findAllById(groupIds);
        return repository.findAllByGroups(groups);
    }

    @Override
    public Member findByEmail(String email) {
        return repository.findByEmail(email);
    }
}