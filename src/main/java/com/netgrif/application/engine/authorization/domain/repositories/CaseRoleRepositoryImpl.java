package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.CaseRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class CaseRoleRepositoryImpl implements CaseRoleRepository{

    private final MongoTemplate mongoTemplate;

    @Override
    public void removeAllByCaseId(String caseId) {
        Query query = buildMatchQuery(Map.of("caseId", caseId));
        mongoTemplate.remove(query, CaseRole.class);
    }

    @Override
    public List<CaseRole> findAll() {
        return mongoTemplate.find(buildBasicQuery(), CaseRole.class);
    }

    @Override
    public CaseRole findByCaseIdAndImportId(String caseId, String importId) {
        Query query = buildMatchQuery(Map.of("caseId", caseId, "importId", importId));
        return mongoTemplate.findOne(query, CaseRole.class);
    }

    private Query buildMatchQuery(Map<String, Object> attributesAndValues) {
        Query query = buildBasicQuery();
        attributesAndValues.forEach((attribute, value) ->
                query.addCriteria(Criteria.where(attribute).is(value)));
        return query;
    }

    private Query buildBasicQuery() {
        return BasicQuery.query(Criteria.where("_class").is(CaseRole.class.getName()));
    }
}
