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

    /**
     * Build a positive view role query using termsQuery for efficiency.
     * This reduces the number of clauses by sending all roles at once.
     */
    private BoolQueryBuilder buildPositiveViewRoleQuery(LoggedUser user) {
        BoolQueryBuilder positiveViewRole = boolQuery();
        if (!user.getProcessRoles().isEmpty()) {
            positiveViewRole.should(termsQuery("viewRoles", user.getProcessRoles()));
        }
        positiveViewRole.minimumShouldMatch(1);
        return positiveViewRole;
    }

    /**
     * Build a negative view role query.
     */
    private BoolQueryBuilder buildNegativeViewRoleQuery(LoggedUser user) {
        BoolQueryBuilder negativeViewRole = boolQuery();
        if (!user.getProcessRoles().isEmpty()) {
            negativeViewRole.should(termsQuery("negativeViewRoles", user.getProcessRoles()));
        }
        negativeViewRole.minimumShouldMatch(1);
        return negativeViewRole;
    }

    /**
     * Build a positive view user query.
     */
    private BoolQueryBuilder buildPositiveViewUser(LoggedUser user) {
        BoolQueryBuilder positiveViewUser = boolQuery();
        positiveViewUser.should(termQuery("viewUsers", user.getId()));
        positiveViewUser.minimumShouldMatch(1);
        return positiveViewUser;
    }

    /**
     * Build a negative view user query.
     */
    private BoolQueryBuilder buildNegativeViewUser(LoggedUser user) {
        BoolQueryBuilder negativeViewUser = boolQuery();
        negativeViewUser.should(termQuery("negativeViewUsers", user.getId()));
        negativeViewUser.minimumShouldMatch(1);
        return negativeViewUser;
    }
}
