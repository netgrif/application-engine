package com.netgrif.workflow.orgstructure.domain;

import org.springframework.data.neo4j.annotation.Query;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface MemberRepository extends Neo4jRepository<Member, Long> {

    @Query("MATCH (m:Member {email: {0}}) WITH m\n" +
            "MATCH (parent:Group)<-[MEMBER_OF]-(m) WITH parent\n" +
            "MATCH (others:Group)-[CHILD_OF*]->(parent) WITH others\n" +
            "MATCH (comembers:Member)-[MEMBER_OF]->(others) RETURN comembers.userId\n" +
            "UNION MATCH (comembers:Member)-[MEMBER_OF]->(parent) RETURN comembers.userId")
    Set<Long> findAllCoMembersIds(String email);

    Member findByEmail(String email);
}