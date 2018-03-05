package com.netgrif.workflow.orgstructure.service;

import com.netgrif.workflow.orgstructure.domain.Member;
import com.netgrif.workflow.orgstructure.domain.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class MemberService implements IMemberService {

    @Autowired
    private MemberRepository repository;

    @Override
    public Member save(Member member) {
        return repository.save(member);
    }

    @Override
    public Set<Long> findAllCoMembersIds(String email) {
        return repository.findAllCoMembersIds(email);
    }

    @Override
    public Member findByEmail(String email) {
        return repository.findByEmail(email);
    }
}