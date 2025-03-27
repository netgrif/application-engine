package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.ApplicationRoleAssignment;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ApplicationRoleAssignmentRepositoryImpl implements ApplicationRoleAssignmentRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<ApplicationRoleAssignment> findAllByActor(String actorId) {
        return mongoTemplate.find(buildMatchQuery("actorId", actorId), ApplicationRoleAssignment.class);
    }

    private Query buildMatchQuery(String field, Object value) {
        return buildBasicQuery().addCriteria(Criteria.where(field).is(value));
    }

    private Query buildBasicQuery() {
        return BasicQuery.query(Criteria.where("_class").is(ApplicationRoleAssignment.class.getName()));
    }
}
