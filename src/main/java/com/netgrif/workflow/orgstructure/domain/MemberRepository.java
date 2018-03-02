package com.netgrif.workflow.orgstructure.domain;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface MemberRepository extends Neo4jRepository<Member, Long> {

    Set<Member> findAllByGroups(Set<Group> groups);

    Member findByEmail(String email);
}