package com.netgrif.application.engine.pfql.service.userresource;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.pfql.domain.enums.QueryType;
import com.netgrif.application.engine.pfql.service.IResourceSearchService;
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserSearchService implements IResourceSearchService<IUser> {
    // todo 2443 javadoc
    // todo 2443 logging

    @Override
    public QueryType getQueryType() {
        return QueryType.USER;
    }

    @Override
    public IUser searchOne(String queryString) {
        return null;
    }

    @Override
    public IUser searchOne(QueryLangEvaluator evaluator) {
        return null;
    }

    @Override
    public Page<IUser> searchAll(String queryString) {
        return null;
    }

    @Override
    public Page<IUser> searchAll(QueryLangEvaluator evaluator) {
        return null;
    }

    @Override
    public long count(String queryString) {
        return 0;
    }

    @Override
    public long count(QueryLangEvaluator evaluator) {
        return 0;
    }

    @Override
    public boolean exists(String queryString) {
        return false;
    }

    @Override
    public boolean exists(QueryLangEvaluator evaluator) {
        return false;
    }
}
