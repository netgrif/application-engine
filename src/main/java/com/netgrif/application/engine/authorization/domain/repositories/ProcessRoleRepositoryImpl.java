package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.ProcessRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class ProcessRoleRepositoryImpl implements ProcessRoleRepository {

    private final MongoTemplate mongoTemplate;

    @Override
    public List<ProcessRole> findAllByImportIdIn(Set<String> importIds) {
        Query query = buildInQuery("importId", importIds);
        return mongoTemplate.find(query, ProcessRole.class);
    }

    @Override
    public List<ProcessRole> findAllByTitle_DefaultValue(String title) {
        Query query = buildMatchQuery("title.defaultValue", title);
        return mongoTemplate.find(query, ProcessRole.class);
    }

    @Override
    public List<ProcessRole> findAllByImportId(String importId) {
        Query query = buildMatchQuery("importId", importId);
        return mongoTemplate.find(query, ProcessRole.class);
    }

    @Override
    public boolean existsByImportId(String importId) {
        Query query = buildMatchQuery("importId", importId);
        return mongoTemplate.exists(query, ProcessRole.class);
    }

    @Override
    public ProcessRole findByImportId(String importId) {
        Query query = buildMatchQuery("importId", importId);
        return mongoTemplate.findOne(query, ProcessRole.class);
    }

    @Override
    public List<ProcessRole> findAll() {
        return mongoTemplate.find(buildBasicQuery(), ProcessRole.class);
    }

    private Query buildInQuery(String field, Collection<?> values) {
        return buildBasicQuery().addCriteria(Criteria.where(field).in(values));
    }

    private Query buildMatchQuery(String field, Object value) {
        return buildBasicQuery().addCriteria(Criteria.where(field).is(value));
    }

    private Query buildBasicQuery() {
        return BasicQuery.query(Criteria.where("_class").is(ProcessRole.class.getName()));
    }
}
