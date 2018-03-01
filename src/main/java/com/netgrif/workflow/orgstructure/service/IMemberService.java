package com.netgrif.workflow.orgstructure.service;

import com.netgrif.workflow.orgstructure.domain.Member;

import java.util.Collection;
import java.util.Set;

public interface IMemberService {

    Member save(Member member);

    Set<Member> findByGroups(Collection<Long> groupIds);
}