package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import org.elasticsearch.index.query.BoolQueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.*;

public abstract class ElasticViewPermissionService {

    protected void buildViewPermissionQuery(BoolQueryBuilder query, LoggedUser user) {
        // Check if viewRoles or viewUserRefs exist
        BoolQueryBuilder viewPermsExists = boolQuery()
                .should(existsQuery("viewRoles"))
                .should(existsQuery("viewUserRefs"));
        // Condition where these attributes do NOT exist
        BoolQueryBuilder viewPermNotExists = boolQuery()
                .mustNot(viewPermsExists);

        /* Build positive view role query */
        BoolQueryBuilder positiveViewRole = buildPositiveViewRoleQuery(viewPermNotExists, user);

        /* Build negative view role query */
        BoolQueryBuilder negativeViewRole = buildNegativeViewRoleQuery(user);

        /* Positive view role set-minus negative view role */
        BoolQueryBuilder positiveRoleSetMinusNegativeRole = setMinus(positiveViewRole, negativeViewRole);

        /* Build positive view userList query */
        BoolQueryBuilder positiveViewUser = buildPositiveViewUser(viewPermNotExists, user);

        /* Role query union positive view userList */
        BoolQueryBuilder roleSetMinusPositiveUserList = union(positiveRoleSetMinusNegativeRole, positiveViewUser);

        /* Build negative view userList query */
        BoolQueryBuilder negativeViewUser = buildNegativeViewUser(user);

        /* Role-UserListPositive set-minus negative view userList */
        BoolQueryBuilder permissionQuery = setMinus(roleSetMinusPositiveUserList, negativeViewUser);

        query.filter(permissionQuery);
    }

    /**
     * Build a positive view role query using termsQuery for efficiency.
     * This reduces the number of clauses by sending all roles at once.
     */
    private BoolQueryBuilder buildPositiveViewRoleQuery(BoolQueryBuilder viewPermNotExists, LoggedUser user) {
        BoolQueryBuilder positiveViewRole = boolQuery();
        if (!user.getProcessRoles().isEmpty()) {
            positiveViewRole.should(termsQuery("viewRoles", user.getProcessRoles()));
        }
        positiveViewRole.should(viewPermNotExists);
        return positiveViewRole;
    }

    /**
     * Build a negative view role query by excluding negative roles.
     */
    private BoolQueryBuilder buildNegativeViewRoleQuery(LoggedUser user) {
        BoolQueryBuilder negativeViewRole = boolQuery();
        if (!user.getProcessRoles().isEmpty()) {
            negativeViewRole.mustNot(termsQuery("negativeViewRoles", user.getProcessRoles()));
        }
        return negativeViewRole;
    }

    /**
     * Build a positive view user query using filter (as score is not needed).
     */
    private BoolQueryBuilder buildPositiveViewUser(BoolQueryBuilder viewPermNotExists, LoggedUser user) {
        return boolQuery()
                .should(viewPermNotExists)
                .filter(termQuery("viewUsers", user.getId()));
    }

    /**
     * Build a negative view user query to exclude the specified user.
     */
    private BoolQueryBuilder buildNegativeViewUser(LoggedUser user) {
        return boolQuery()
                .mustNot(termQuery("negativeViewUsers", user.getId()));
    }

    private BoolQueryBuilder setMinus(BoolQueryBuilder positiveSet, BoolQueryBuilder negativeSet) {
        BoolQueryBuilder positiveSetMinusNegativeSet = boolQuery();
        positiveSetMinusNegativeSet.must(positiveSet);
        positiveSetMinusNegativeSet.must(negativeSet);
        return positiveSetMinusNegativeSet;
    }

    /**
     * Unions two queries using OR with a minimum_should_match of 1.
     */
    private BoolQueryBuilder union(BoolQueryBuilder setA, BoolQueryBuilder setB) {
        return boolQuery()
                .should(setA)
                .should(setB)
                .minimumShouldMatch(1);
    }
}
