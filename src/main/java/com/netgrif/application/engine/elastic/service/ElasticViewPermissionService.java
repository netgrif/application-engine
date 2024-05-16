package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import static org.springframework.data.elasticsearch.client.elc.Queries.termQuery;

public abstract class ElasticViewPermissionService {

    protected void buildViewPermissionQuery(BoolQuery.Builder query, LoggedUser user) {
        BoolQuery.Builder viewPermsExists = new BoolQuery.Builder();
        BoolQuery.Builder viewPermNotExists = new BoolQuery.Builder();

        viewPermsExists.should(should -> should.exists(ExistsQuery.of(builder -> builder.field("viewRoles"))));
        viewPermsExists.should(should -> should.exists(ExistsQuery.of(builder -> builder.field("viewUserRefs"))));
        viewPermNotExists.mustNot(mustNot -> mustNot.bool(viewPermsExists.build()));

        /* Build positive view role query */
        BoolQuery.Builder positiveViewRole = buildPositiveViewRoleQuery(viewPermNotExists, user);

        /* Build negative view role query */
        BoolQuery.Builder negativeViewRole = buildNegativeViewRoleQuery(user);

        /* Positive view role set-minus negative view role */
        BoolQuery.Builder positiveRoleSetMinusNegativeRole = setMinus(positiveViewRole, negativeViewRole);

        /* Build positive view userList query */
        BoolQuery.Builder positiveViewUser = buildPositiveViewUser(viewPermNotExists, user);

        /* Role query union positive view userList */
        BoolQuery.Builder roleSetMinusPositiveUserList = union(positiveRoleSetMinusNegativeRole, positiveViewUser);

        /* Build negative view userList query */
        BoolQuery.Builder negativeViewUser = buildNegativeViewUser(user);

        /* Role-UserListPositive set-minus negative view userList */
        BoolQuery.Builder permissionQuery = setMinus(roleSetMinusPositiveUserList, negativeViewUser);

        query.filter(permissionQuery.build()._toQuery());
    }

    private BoolQuery.Builder buildPositiveViewRoleQuery(BoolQuery.Builder viewPermNotExists, LoggedUser user) {
        BoolQuery.Builder positiveViewRole = new BoolQuery.Builder();
        BoolQuery.Builder positiveViewRoleQuery = new BoolQuery.Builder();
        for (String roleId : user.getProcessRoles()) {
            positiveViewRoleQuery.should(termQuery("viewRoles", roleId)._toQuery());
        }
        positiveViewRole.should(viewPermNotExists.build()._toQuery());
        positiveViewRole.should(positiveViewRoleQuery.build()._toQuery());
        return positiveViewRole;
    }

    private BoolQuery.Builder buildNegativeViewRoleQuery(LoggedUser user) {
        BoolQuery.Builder negativeViewRole = new BoolQuery.Builder();
        BoolQuery.Builder negativeViewRoleQuery = new BoolQuery.Builder();
        for (String roleId : user.getProcessRoles()) {
            negativeViewRoleQuery.should(termQuery("negativeViewRoles", roleId)._toQuery());
        }
        negativeViewRole.mustNot(negativeViewRoleQuery.build()._toQuery());
        return negativeViewRole;
    }

    private BoolQuery.Builder buildPositiveViewUser(BoolQuery.Builder  viewPermNotExists, LoggedUser user) {
        BoolQuery.Builder positiveViewUser = new BoolQuery.Builder();
        BoolQuery.Builder positiveViewUserQuery = new BoolQuery.Builder();
        positiveViewUserQuery.must(termQuery("viewUsers", user.getId())._toQuery());
        positiveViewUser.should(viewPermNotExists.build()._toQuery());
        positiveViewUser.should(positiveViewUserQuery.build()._toQuery());
        return positiveViewUser;
    }

    private BoolQuery.Builder buildNegativeViewUser(LoggedUser user) {
        BoolQuery.Builder negativeViewUser = new BoolQuery.Builder();
        BoolQuery.Builder negativeViewUserQuery = new BoolQuery.Builder();
        negativeViewUserQuery.should(termQuery("negativeViewUsers", user.getId())._toQuery());
        negativeViewUser.mustNot(negativeViewUserQuery.build()._toQuery());
        return negativeViewUser;
    }

    private BoolQuery.Builder setMinus(BoolQuery.Builder positiveSet, BoolQuery.Builder negativeSet) {
        BoolQuery.Builder positiveSetMinusNegativeSet = new BoolQuery.Builder();
        positiveSetMinusNegativeSet.must(positiveSet.build()._toQuery());
        positiveSetMinusNegativeSet.must(negativeSet.build()._toQuery());
        return positiveSetMinusNegativeSet;
    }

    private BoolQuery.Builder union(BoolQuery.Builder setA, BoolQuery.Builder setB) {
        BoolQuery.Builder unionSet = new BoolQuery.Builder();
        unionSet.should(setA.build()._toQuery());
        unionSet.should(setB.build()._toQuery());
        return unionSet;
    }
}
