package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import org.elasticsearch.index.query.BoolQueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.*;

public abstract class ElasticViewPermissionService {

    protected void buildViewPermissionQuery(BoolQueryBuilder query, LoggedUser user) {

//        (Rp!=0 & Rn = 0)
        BoolQueryBuilder roleViewQuery = boolQuery()
                .filter(buildPositiveViewRoleQuery(user))
                .mustNot(buildNegativeViewRoleQuery(user));

//        ((Rp!=0 & Rn = 0) or Up!=0)
        BoolQueryBuilder roleOrPositiveUserQuery = boolQuery().should(roleViewQuery)
                .should(buildPositiveViewUser(user))
                .minimumShouldMatch(1);

//        (((Rp!=0 & Rn = 0) or Up!=0) & Un=0) == 1
        query.filter(roleOrPositiveUserQuery)
                .mustNot(buildNegativeViewUser(user));
    }

    private BoolQueryBuilder buildPositiveViewRoleQuery(LoggedUser user) {
        BoolQueryBuilder positiveViewRole = boolQuery();
        BoolQueryBuilder positiveViewRoleQuery = boolQuery();
        for (String roleId : user.getProcessRoles()) {
            positiveViewRoleQuery.should(termQuery("viewRoles", roleId));
        }
        positiveViewRole.should(positiveViewRoleQuery);
        positiveViewRole.minimumShouldMatch(1);
        return positiveViewRole;
    }

    private BoolQueryBuilder buildNegativeViewRoleQuery(LoggedUser user) {
        BoolQueryBuilder negativeViewRoleQuery = boolQuery();
        for (String roleId : user.getProcessRoles()) {
            negativeViewRoleQuery.should(termQuery("negativeViewRoles", roleId));
        }
        negativeViewRoleQuery.minimumShouldMatch(1);
        return negativeViewRoleQuery;
    }

    private BoolQueryBuilder buildPositiveViewUser(LoggedUser user) {
        BoolQueryBuilder positiveViewUser = boolQuery();
        BoolQueryBuilder positiveViewUserQuery = boolQuery();
        positiveViewUserQuery.must(termQuery("viewUsers", user.getId()));
        positiveViewUser.should(positiveViewUserQuery);
        positiveViewUser.minimumShouldMatch(1);
        return positiveViewUser;
    }

    private BoolQueryBuilder buildNegativeViewUser(LoggedUser user) {
        BoolQueryBuilder negativeViewUserQuery = boolQuery();
        negativeViewUserQuery.should(termQuery("negativeViewUsers", user.getId()));
        negativeViewUserQuery.minimumShouldMatch(1);
        return negativeViewUserQuery;
    }
}
