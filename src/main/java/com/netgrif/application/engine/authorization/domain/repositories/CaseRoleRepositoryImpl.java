package com.netgrif.application.engine.authorization.domain.repositories;

import com.netgrif.application.engine.authorization.domain.CaseRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CaseRoleRepositoryImpl implements CaseRoleRepository{

    private final MongoTemplate mongoTemplate;

    @Override
    public void removeAllByCaseId(String caseId) {
        Query query = buildMatchQuery("caseId", caseId);
        mongoTemplate.remove(query, CaseRole.class);
    }

    @Override
    public List<CaseRole> findAll() {
        return mongoTemplate.find(buildBasicQuery(), CaseRole.class);
    }

    private Query buildMatchQuery(String field, Object value) {
        return buildBasicQuery().addCriteria(Criteria.where(field).is(value));
    }

    private Query buildBasicQuery() {
        return BasicQuery.query(Criteria.where("_class").is(CaseRole.class.getName()));
    }
}
