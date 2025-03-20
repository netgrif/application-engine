package com.netgrif.application.engine.elastic.service;

import com.netgrif.application.engine.authorization.domain.RoleAssignment;
import com.netgrif.application.engine.authorization.service.RoleAssignmentService;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.elasticsearch.index.query.QueryBuilders.*;

public abstract class ElasticViewPermissionService {

    @Autowired
    protected RoleAssignmentService roleAssignmentService;

    /**
     * todo javadoc
     * */
    protected void buildViewPermissionQuery(BoolQueryBuilder query, String actorId) {
        // Check if processRoles or caseRoles exist
        BoolQueryBuilder viewPermsExists = boolQuery()
                .should(existsQuery("viewProcessRoles"))
                .should(existsQuery("viewCaseRoles"));
        // Condition where these attributes do NOT exist
        BoolQueryBuilder viewPermNotExists = boolQuery()
                .mustNot(viewPermsExists);

        // Collect assigned roles to user
        List<RoleAssignment> assignments = roleAssignmentService.findAllByActorId(actorId);
        final Set<String> assignedRoleIds = assignments.stream().map(RoleAssignment::getRoleId).collect(toUnmodifiableSet());

        // Build queries for each role type
        BoolQueryBuilder positiveProcessRole = buildPositiveProcessRoleQuery(viewPermNotExists, assignedRoleIds);
        BoolQueryBuilder negativeProcessRole = buildNegativeProcessRoleQuery(assignedRoleIds);
        BoolQueryBuilder positiveCaseRole = buildPositiveCaseRoleQuery(viewPermNotExists, assignedRoleIds);
        BoolQueryBuilder negativeCaseRole = buildNegativeCaseRoleQuery(assignedRoleIds);
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
    private BoolQueryBuilder buildPositiveProcessRoleQuery(BoolQueryBuilder viewPermNotExists, Set<String> roleIds) {
        BoolQueryBuilder positiveProcessRole = boolQuery();
        if (!roleIds.isEmpty()) {
            positiveProcessRole.should(termsQuery("positiveViewProcessRoles", roleIds));
        }
        positiveProcessRole.should(viewPermNotExists);
        return positiveProcessRole;
    }

    /**
     * todo javadoc
     * Build a negative view role query by excluding negative roles.
     */
    private BoolQueryBuilder buildNegativeProcessRoleQuery(Set<String> roleIds) {
        BoolQueryBuilder negativeProcessRole = boolQuery();
        if (!roleIds.isEmpty()) {
            negativeProcessRole.mustNot(termsQuery("negativeViewProcessRoles", roleIds));
        }
        return negativeProcessRole;
    }

    /**
     * todo javadoc
     * Build a positive view user query using filter (as score is not needed).
     */
    private BoolQueryBuilder buildPositiveCaseRoleQuery(BoolQueryBuilder viewPermNotExists, Set<String> roleIds) {
        BoolQueryBuilder positiveCaseRole = boolQuery();
        if (!roleIds.isEmpty()) {
            positiveCaseRole.should(termsQuery("positiveViewCaseRoles", roleIds));
        }
        positiveCaseRole.should(viewPermNotExists);
        return positiveCaseRole;
    }

    /**
     * todo javadoc
     * */
    private BoolQueryBuilder buildNegativeCaseRoleQuery(Set<String> roleIds) {
        BoolQueryBuilder negativeCaseRole = boolQuery();
        if (!roleIds.isEmpty()) {
            negativeCaseRole.mustNot(termsQuery("negativeViewCaseRoles", roleIds));
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
