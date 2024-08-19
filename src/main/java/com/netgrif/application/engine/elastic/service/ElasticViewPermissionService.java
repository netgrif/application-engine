package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import static org.springframework.data.elasticsearch.client.elc.Queries.termQuery;

public abstract class ElasticViewPermissionService {

    protected void buildViewPermissionQuery(BoolQuery.Builder query, LoggedUser user) {
        BoolQuery.Builder viewPermsExists = new BoolQuery.Builder();
        BoolQuery.Builder viewPermNotExistsBuilder = new BoolQuery.Builder();

        viewPermsExists.should(should -> should.exists(ExistsQuery.of(builder -> builder.field("viewRoles"))));
        viewPermsExists.should(should -> should.exists(ExistsQuery.of(builder -> builder.field("viewUserRefs"))));
        BoolQuery viewPermNotExists = viewPermNotExistsBuilder.mustNot(mustNot -> mustNot.bool(viewPermsExists.build())).build();

        /* Build positive view role query */
        BoolQuery positiveViewRole = buildPositiveViewRoleQuery(viewPermNotExists, user);

        /* Build negative view role query */
        BoolQuery negativeViewRole = buildNegativeViewRoleQuery(user);

        /* Positive view role set-minus negative view role */
        BoolQuery positiveRoleSetMinusNegativeRole = setMinus(positiveViewRole, negativeViewRole);

        /* Build positive view userList query */
        BoolQuery positiveViewUser = buildPositiveViewUser(viewPermNotExists, user);

        /* Role query union positive view userList */
        BoolQuery roleSetMinusPositiveUserList = union(positiveRoleSetMinusNegativeRole, positiveViewUser);

        /* Build negative view userList query */
        BoolQuery negativeViewUser = buildNegativeViewUser(user);

        /* Role-UserListPositive set-minus negative view userList */
        BoolQuery permissionQuery = setMinus(roleSetMinusPositiveUserList, negativeViewUser);

        query.filter(permissionQuery._toQuery());
    }

    private BoolQuery buildPositiveViewRoleQuery(BoolQuery viewPermNotExists, LoggedUser user) {
        BoolQuery.Builder positiveViewRole = new BoolQuery.Builder();
        BoolQuery.Builder positiveViewRoleQuery = new BoolQuery.Builder();
        for (String roleId : user.getProcessRoles()) {
            positiveViewRoleQuery.should(termQuery("viewRoles", roleId)._toQuery());
        }
        positiveViewRole.should(viewPermNotExists._toQuery());
        positiveViewRole.should(positiveViewRoleQuery.build()._toQuery());
        return positiveViewRole.build();
    }

    private BoolQuery buildNegativeViewRoleQuery(LoggedUser user) {
        BoolQuery.Builder negativeViewRole = new BoolQuery.Builder();
        BoolQuery.Builder negativeViewRoleQuery = new BoolQuery.Builder();
        for (String roleId : user.getProcessRoles()) {
            negativeViewRoleQuery.should(termQuery("negativeViewRoles", roleId)._toQuery());
        }
        negativeViewRole.mustNot(negativeViewRoleQuery.build()._toQuery());
        return negativeViewRole.build();
    }

    private BoolQuery buildPositiveViewUser(BoolQuery viewPermNotExists, LoggedUser user) {
        BoolQuery.Builder positiveViewUser = new BoolQuery.Builder();
        BoolQuery.Builder positiveViewUserQuery = new BoolQuery.Builder();
        positiveViewUserQuery.must(termQuery("viewUsers", user.getId())._toQuery());
        positiveViewUser.should(viewPermNotExists._toQuery());
        positiveViewUser.should(positiveViewUserQuery.build()._toQuery());
        return positiveViewUser.build();
    }

    private BoolQuery buildNegativeViewUser(LoggedUser user) {
        BoolQuery.Builder negativeViewUser = new BoolQuery.Builder();
        BoolQuery.Builder negativeViewUserQuery = new BoolQuery.Builder();
        negativeViewUserQuery.should(termQuery("negativeViewUsers", user.getId())._toQuery());
        negativeViewUser.mustNot(negativeViewUserQuery.build()._toQuery());
        return negativeViewUser.build();
    }

    private BoolQuery setMinus(BoolQuery positiveSet, BoolQuery negativeSet) {
        BoolQuery.Builder positiveSetMinusNegativeSet = new BoolQuery.Builder();
        positiveSetMinusNegativeSet.must(positiveSet._toQuery());
        positiveSetMinusNegativeSet.must(negativeSet._toQuery());
        return positiveSetMinusNegativeSet.build();
    }

    private BoolQuery union(BoolQuery setA, BoolQuery setB) {
        BoolQuery.Builder unionSet = new BoolQuery.Builder();
        unionSet.should(setA._toQuery());
        unionSet.should(setB._toQuery());
        return unionSet.build();
    }
}
