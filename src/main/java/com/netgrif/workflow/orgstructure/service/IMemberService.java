package com.netgrif.workflow.orgstructure.service;

import com.netgrif.workflow.orgstructure.domain.Member;

import java.util.Set;

public interface IMemberService {

    Member save(Member member);

    Set<Long> findAllCoMembersIds(String email);

    Member findByEmail(String email);
}