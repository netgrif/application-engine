package com.netgrif.application.engine.elastic.service;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.ExistsQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders;
import co.elastic.clients.elasticsearch._types.query_dsl.TermsQueryField;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.springframework.data.elasticsearch.client.elc.Queries.termQuery;

public abstract class ElasticViewPermissionService {

    protected void buildViewPermissionQuery(BoolQuery.Builder query, LoggedUser user) {
        BoolQuery.Builder viewPermsExists = new BoolQuery.Builder()
                .should(should -> should.exists(ExistsQuery.of(builder -> builder.field("viewRoles"))))
                .should(should -> should.exists(ExistsQuery.of(builder -> builder.field("viewUserRefs"))));
        BoolQuery.Builder viewPermNotExistsBuilder = new BoolQuery.Builder()
                .mustNot(mustNot -> mustNot.bool(viewPermsExists.build()));

        BoolQuery viewPermNotExists = viewPermNotExistsBuilder.build();

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

        TermsQueryField roleIds = new TermsQueryField.Builder()
                .value(user.getProcessRoles().stream().map(ProcessRole::getStringId).map(FieldValue::of).collect(Collectors.toList()))
                .build();

        positiveViewRoleQuery.should(QueryBuilders.terms(term -> term.field("viewRoles").terms(roleIds)));

        positiveViewRole.should(viewPermNotExists._toQuery());
        positiveViewRole.should(positiveViewRoleQuery.build()._toQuery());
        return positiveViewRole.build();
    }

    private BoolQuery buildNegativeViewRoleQuery(LoggedUser user) {
        BoolQuery.Builder negativeViewRole = new BoolQuery.Builder();
        BoolQuery.Builder negativeViewRoleQuery = new BoolQuery.Builder();

        TermsQueryField roleIds = new TermsQueryField.Builder()
                .value(user.getProcessRoles().stream().map(ProcessRole::getStringId).map(FieldValue::of).collect(Collectors.toList()))
                .build();

        negativeViewRoleQuery.should(QueryBuilders.terms(term -> term.field("negativeViewRoles").terms(roleIds)));
        negativeViewRole.mustNot(negativeViewRoleQuery.build()._toQuery());
        return negativeViewRole.build();
    }

    private BoolQuery buildPositiveViewUser(BoolQuery viewPermNotExists, LoggedUser user) {
        return new BoolQuery.Builder()
                .should(viewPermNotExists._toQuery())
                .filter(termQuery("viewUsers", user.getStringId())._toQuery())
                .build();
    }

    private BoolQuery buildNegativeViewUser(LoggedUser user) {
        return new BoolQuery.Builder()
                .mustNot(termQuery("negativeViewUsers", user.getStringId())._toQuery())
                .build();
    }

    private BoolQuery setMinus(BoolQuery positiveSet, BoolQuery negativeSet) {
        return new BoolQuery.Builder()
                .must(positiveSet._toQuery())
                .must(negativeSet._toQuery())
                .build();
    }

    private BoolQuery union(BoolQuery setA, BoolQuery setB) {
        return new BoolQuery.Builder()
                .should(setA._toQuery())
                .should(setB._toQuery())
                .minimumShouldMatch(String.valueOf(1))
                .build();
    }
}
