package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.ApplicationRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ApplicationRoleRepositoryImpl implements ApplicationRoleRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public boolean existsByImportId(String importId) {
        Query query = buildMatchQuery("importId", importId);
        return mongoTemplate.exists(query, ApplicationRole.class);
    }

    @Override
    public ApplicationRole findByImportId(String importId) {
        Query query = buildMatchQuery("importId", importId);
        return mongoTemplate.findOne(query, ApplicationRole.class);
    }

    @Override
    public List<ApplicationRole> findAll() {
        return mongoTemplate.find(buildBasicQuery(), ApplicationRole.class);
    }

    private Query buildInQuery(String field, Collection<?> values) {
        return buildBasicQuery().addCriteria(Criteria.where(field).in(values));
    }

    private Query buildMatchQuery(String field, Object value) {
        return buildBasicQuery().addCriteria(Criteria.where(field).is(value));
    }

    private Query buildBasicQuery() {
        return BasicQuery.query(Criteria.where("_class").is(ApplicationRole.class.getName()));
    }
}
