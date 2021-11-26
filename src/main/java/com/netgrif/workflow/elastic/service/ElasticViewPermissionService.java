package com.netgrif.workflow.elastic.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import org.elasticsearch.index.query.BoolQueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.*;

public abstract class ElasticViewPermissionService {

    protected void buildViewPermissionQuery(BoolQueryBuilder query, LoggedUser user) {
        BoolQueryBuilder permissionQuery = boolQuery();
        BoolQueryBuilder positiveRoleSetMinusNegativeRole = boolQuery();
        BoolQueryBuilder roleSetMinusPositiveUserList = boolQuery();

        /* Build positive view role query */
        BoolQueryBuilder positiveViewRole = boolQuery();
        BoolQueryBuilder viewRoleNotExists = boolQuery();
        BoolQueryBuilder positiveViewRoleQuery = boolQuery();
        for (String roleId : user.getProcessRoles()) {
            positiveViewRoleQuery.should(termQuery("viewRoles", roleId));
        }
        viewRoleNotExists.mustNot(existsQuery("viewRoles"));
        positiveViewRole.should(viewRoleNotExists);
        positiveViewRole.should(positiveViewRoleQuery);

        /* Build negative view role query */
        BoolQueryBuilder negativeViewRole = boolQuery();
        BoolQueryBuilder negativeViewRoleQuery = boolQuery();
        for (String roleId : user.getProcessRoles()) {
            negativeViewRoleQuery.should(termQuery("negativeViewRoles", roleId));
        }
        negativeViewRole.mustNot(negativeViewRoleQuery);

        /* Positive view role set-minus negative view role */
        positiveRoleSetMinusNegativeRole.must(positiveViewRole);
        positiveRoleSetMinusNegativeRole.must(negativeViewRole);

        /* Build positive view userList query */
        BoolQueryBuilder positiveViewUser = boolQuery();
        BoolQueryBuilder viewUserRefExists = boolQuery();
        BoolQueryBuilder positiveViewUserQuery = boolQuery();

        positiveViewUserQuery.must(termQuery("viewUsers", user.getId()));
        viewUserRefExists.mustNot(existsQuery("viewUserRefs"));
        positiveViewUser.should(viewUserRefExists);
        positiveViewUser.should(positiveViewUserQuery);

        /* Role query union positive view userList */
        roleSetMinusPositiveUserList.should(positiveRoleSetMinusNegativeRole);
        roleSetMinusPositiveUserList.should(positiveViewUser);

        /* Build negative view userList query */
        BoolQueryBuilder negativeViewUser = boolQuery();
        BoolQueryBuilder negativeViewUserQuery = boolQuery();
        negativeViewUserQuery.should(termQuery("negativeViewUsers", user.getId()));
        negativeViewUser.mustNot(negativeViewUserQuery);

        /* Role-UserListPositive set-minus negative view userList */
        permissionQuery.must(roleSetMinusPositiveUserList);
        permissionQuery.must(negativeViewUser);

        query.filter(permissionQuery);
    }
}
