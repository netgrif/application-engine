package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.authentication.domain.LoggedUser;
import org.elasticsearch.index.query.BoolQueryBuilder;

import static org.elasticsearch.index.query.QueryBuilders.*;

public abstract class ElasticViewPermissionService {

    /**
     * todo javadoc
     * */
    protected void buildViewPermissionQuery(BoolQueryBuilder query, LoggedUser user) {
        // Check if processRoles or caseRoles exist
        BoolQueryBuilder viewPermsExists = boolQuery()
                .should(existsQuery("viewProcessRoles"))
                .should(existsQuery("viewCaseRoles"));
        // Condition where these attributes do NOT exist
        BoolQueryBuilder viewPermNotExists = boolQuery()
                .mustNot(viewPermsExists);

        // Build queries for each role type
        BoolQueryBuilder positiveProcessRole = buildPositiveProcessRoleQuery(viewPermNotExists, user);
        BoolQueryBuilder negativeProcessRole = buildNegativeProcessRoleQuery(user);
        BoolQueryBuilder positiveCaseRole = buildPositiveCaseRoleQuery(viewPermNotExists, user);
        BoolQueryBuilder negativeCaseRole = buildNegativeCaseRoleQuery(user);

        // Calculate final query
        BoolQueryBuilder permissionQuery = collectQueriesByFormula(positiveProcessRole, negativeProcessRole,
                positiveCaseRole, negativeCaseRole);
        query.filter(permissionQuery);
    }

    /**
     * todo javadoc
     * Build a positive view role query using termsQuery for efficiency.
     * This reduces the number of clauses by sending all roles at once.
     */
    private BoolQueryBuilder buildPositiveProcessRoleQuery(BoolQueryBuilder viewPermNotExists, LoggedUser user) {
        BoolQueryBuilder positiveProcessRole = boolQuery();
        if (!user.getRoles().isEmpty()) {
            positiveProcessRole.should(termsQuery("positiveViewProcessRoles", user.getRoles()));
        }
        positiveProcessRole.should(viewPermNotExists);
        return positiveProcessRole;
    }

    /**
     * todo javadoc
     * Build a negative view role query by excluding negative roles.
     */
    private BoolQueryBuilder buildNegativeProcessRoleQuery(LoggedUser user) {
        BoolQueryBuilder negativeProcessRole = boolQuery();
        if (!user.getRoles().isEmpty()) {
            negativeProcessRole.mustNot(termsQuery("negativeViewProcessRoles", user.getRoles()));
        }
        return negativeProcessRole;
    }

    /**
     * todo javadoc
     * Build a positive view user query using filter (as score is not needed).
     */
    private BoolQueryBuilder buildPositiveCaseRoleQuery(BoolQueryBuilder viewPermNotExists, LoggedUser user) {
        BoolQueryBuilder positiveCaseRole = boolQuery();
        if (!user.getRoles().isEmpty()) {
            positiveCaseRole.should(termsQuery("positiveViewCaseRoles", user.getRoles()));
        }
        positiveCaseRole.should(viewPermNotExists);
        return positiveCaseRole;
    }

    /**
     * todo javadoc
     * */
    private BoolQueryBuilder buildNegativeCaseRoleQuery(LoggedUser user) {
        BoolQueryBuilder negativeCaseRole = boolQuery();
        if (!user.getRoles().isEmpty()) {
            negativeCaseRole.mustNot(termsQuery("negativeViewCaseRoles", user.getRoles()));
        }
        return negativeCaseRole;
    }

    /**
     * todo javadoc
     * Calculate resulting condition, which must be matched in order to search document
     * $$((R_{p} \setminus R_{n}) \cup U_{p}) \setminus U_{n}$$
     * $\setminus$ - seminus, e.g. $A \setminus B$ = every element from A that is not in B
     * $\cup$ - union of sets
     * $R_{p}$ - set of roles that are assigned to user and define given permission with true (grant the permission)
     * $R_{n}$ - set of roles that are assigned to user and define given permission with false (forbid the permission)
     * $U_{p}$ - set of user lists that user is part of and define given permission with true (grant the permission)
     * $U_{n}$ - set of user lists that user is part of and define given permission with false (forbid the permission)
     * */
    private BoolQueryBuilder collectQueriesByFormula(BoolQueryBuilder positiveProcessRole, BoolQueryBuilder negativeProcessRole,
                                                     BoolQueryBuilder positiveCaseRole, BoolQueryBuilder negativeCaseRole) {
        BoolQueryBuilder processRoleDiff = setMinus(positiveProcessRole, negativeProcessRole);
        BoolQueryBuilder processRoleDiffUnionPositiveCaseRole = union(processRoleDiff, positiveCaseRole);
        return setMinus(processRoleDiffUnionPositiveCaseRole, negativeCaseRole);
    }

    /**
     * todo javadoc
     * */
    private BoolQueryBuilder setMinus(BoolQueryBuilder positiveSet, BoolQueryBuilder negativeSet) {
        return boolQuery()
                .must(positiveSet)
                .must(negativeSet);
    }

    /**
     * todo javadoc
     * Unions two queries using OR with a minimum_should_match of 1.
     */
    private BoolQueryBuilder union(BoolQueryBuilder setA, BoolQueryBuilder setB) {
        return boolQuery()
                .should(setA)
                .should(setB)
                .minimumShouldMatch(1);
    }
}
