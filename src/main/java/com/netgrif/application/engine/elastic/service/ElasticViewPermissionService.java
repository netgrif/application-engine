package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import org.elasticsearch.index.query.BoolQueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.*;

public abstract class ElasticViewPermissionService {

    protected void buildViewPermissionQuery(BoolQueryBuilder query, LoggedUser user) {
        BoolQueryBuilder viewPermsExists = boolQuery();
        BoolQueryBuilder viewPermNotExists = boolQuery();
        viewPermsExists.should(existsQuery("viewRoles"));
        viewPermsExists.should(existsQuery("viewUserRefs"));
        viewPermNotExists.mustNot(viewPermsExists);

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

    private BoolQueryBuilder buildPositiveViewRoleQuery(BoolQueryBuilder viewPermNotExists, LoggedUser user) {
        BoolQueryBuilder positiveViewRole = boolQuery();
        BoolQueryBuilder positiveViewRoleQuery = boolQuery();
        for (String roleId : user.getProcessRoles()) {
            positiveViewRoleQuery.should(termQuery("viewRoles", roleId));
        }
        positiveViewRole.should(viewPermNotExists);
        positiveViewRole.should(positiveViewRoleQuery);
        return positiveViewRole;
    }

    private BoolQueryBuilder buildNegativeViewRoleQuery(LoggedUser user) {
        BoolQueryBuilder negativeViewRole = boolQuery();
        BoolQueryBuilder negativeViewRoleQuery = boolQuery();
        for (String roleId : user.getProcessRoles()) {
            negativeViewRoleQuery.should(termQuery("negativeViewRoles", roleId));
        }
        negativeViewRole.mustNot(negativeViewRoleQuery);
        return negativeViewRole;
    }

    private BoolQueryBuilder buildPositiveViewUser(BoolQueryBuilder viewPermNotExists, LoggedUser user) {
        BoolQueryBuilder positiveViewUser = boolQuery();
        BoolQueryBuilder positiveViewUserQuery = boolQuery();
        positiveViewUserQuery.must(termQuery("viewUsers", user.getId()));
        positiveViewUser.should(viewPermNotExists);
        positiveViewUser.should(positiveViewUserQuery);
        return positiveViewUser;
    }

    private BoolQueryBuilder buildNegativeViewUser(LoggedUser user) {
        BoolQueryBuilder negativeViewUser = boolQuery();
        BoolQueryBuilder negativeViewUserQuery = boolQuery();
        negativeViewUserQuery.should(termQuery("negativeViewUsers", user.getId()));
        negativeViewUser.mustNot(negativeViewUserQuery);
        return negativeViewUser;
    }

    private BoolQueryBuilder setMinus(BoolQueryBuilder positiveSet, BoolQueryBuilder negativeSet) {
        BoolQueryBuilder positiveSetMinusNegativeSet = boolQuery();
        positiveSetMinusNegativeSet.must(positiveSet);
        positiveSetMinusNegativeSet.must(negativeSet);
        return positiveSetMinusNegativeSet;
    }

    private BoolQueryBuilder union(BoolQueryBuilder setA, BoolQueryBuilder setB) {
        BoolQueryBuilder unionSet = boolQuery();
        unionSet.should(setA);
        unionSet.should(setB);
        return unionSet;
    }
}
