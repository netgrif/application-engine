package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.workflow.domain.QTask;
import com.netgrif.application.engine.workflow.domain.State;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.taskSearch.TaskSearchCaseRequest;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskSearchService extends MongoSearchService<Task> {

    public Predicate buildQuery(List<TaskSearchRequest> requests, String actorId, Locale locale, Boolean isIntersection) {
        List<Predicate> singleQueries = requests.stream().map(r -> this.buildSingleQuery(r, actorId, locale)).collect(Collectors.toList());

        if (isIntersection && !singleQueries.stream().allMatch(Objects::nonNull)) {
            // one of the queries evaluates to empty set => the entire result is an empty set
            return null;
        } else if (!isIntersection) {
            singleQueries = singleQueries.stream().filter(Objects::nonNull).collect(Collectors.toList());
            if (singleQueries.isEmpty()) {
                // all queries result in an empty set => the entire result is an empty set
                return null;
            }
        }

        BooleanBuilder builder = constructPredicateTree(singleQueries, isIntersection ? BooleanBuilder::and : BooleanBuilder::or);

//        final Set<String> assignedRoleIds = roleAssignmentService.findAllRoleIdsByActorAndGroups(actorId);
//        BooleanBuilder constraints = new BooleanBuilder(buildProcessRolesQueryConstraint(assignedRoleIds));
//        constraints.or(buildCaseRolesQueryConstraint(assignedRoleIds));
//        builder.and(constraints);
//        BooleanBuilder permissionConstraints = new BooleanBuilder(buildPositiveProcessRoleQueryConstraint(assignedRoleIds));
//        permissionConstraints.andNot(buildNegativeProcessRoleQueryConstraint(assignedRoleIds));
//        permissionConstraints.or(buildPositiveCaseRoleQueryConstraint(assignedRoleIds));
//        permissionConstraints.andNot(buildNegativeCaseRoleQueryConstraint(assignedRoleIds));
//        builder.and(permissionConstraints);
        return builder;
    }

//    protected Predicate buildProcessRolesQueryConstraint(Set<String> assignedRoleIds) {
//        List<Predicate> roleConstraints = assignedRoleIds.stream().map(this::processRoleQuery).collect(Collectors.toList());
//        return constructPredicateTree(roleConstraints, BooleanBuilder::or);
//    }
//
//    protected Predicate buildCaseRolesQueryConstraint(Set<String> assignedRoleIds) {
//        List<Predicate> roleConstraints = assignedRoleIds.stream().map(this::caseRoleQuery).collect(Collectors.toList());
//        return constructPredicateTree(roleConstraints, BooleanBuilder::or);
//    }
//
//    protected Predicate buildPositiveProcessRoleQueryConstraint(Set<String> assignedRoleIds) {
//        List<Predicate> roleConstraints = assignedRoleIds.stream().map(this::positiveProcessRoleQuery).collect(Collectors.toList());
//        return constructPredicateTree(roleConstraints, BooleanBuilder::or);
//    }
//
//    protected Predicate buildPositiveCaseRoleQueryConstraint(Set<String> assignedRoleIds) {
//        Predicate userConstraints = positiveCaseRoleQuery(user.getId());
//        return constructPredicateTree(Collections.singletonList(userConstraints), BooleanBuilder::or);
//    }
//
//    protected Predicate buildNegativeProcessRoleQueryConstraint(Set<String> assignedRoleIds) {
//        List<Predicate> roleConstraints = user.getProcessRoles().stream().map(this::negativeViewRoleQuery).collect(Collectors.toList());
//        return constructPredicateTree(roleConstraints, BooleanBuilder::or);
//    }
//
//    protected Predicate buildNegativeCaseRoleQueryConstraint(Set<String> assignedRoleIds) {
//        Predicate userConstraints = negativeViewUsersQuery(user.getId());
//        return constructPredicateTree(Collections.singletonList(userConstraints), BooleanBuilder::or);
//    }
//
//    public Predicate positiveProcessRoleQuery(String roleId) {
//        // todo: release/8.0.0 view_disabled
//        return QTask.task.caseRolePermissions.permissions.isEmpty().and(QTask.task.processRolePermissions.permissions.isEmpty())
//                .or(QTask.task.processRolePermissions.permissions.get(roleId).eq(Map.of(TaskPermission.VIEW, true)));
//    }
//
//    public Predicate positiveCaseRoleQuery(String userId) {
//        return QTask.task.caseRolePermissions.permissions.isEmpty().and(QTask.task.processRolePermissions.permissions.isEmpty())
//                .or(QTask.task.processRolePermissions.permissions.get(roleId).eq(Map.of(TaskPermission.VIEW, true)));
//        return QTask.task.negativeViewRoles.isEmpty().and(QTask.task.viewUserRefs.isEmpty()).and(QTask.task.viewRoles.isEmpty()).or(QTask.task.viewUsers.contains(userId));
//    }
//
//    public Predicate negativeViewRoleQuery(String role) {
//        return QTask.task.negativeViewRoles.contains(role);
//    }
//
//    public Predicate negativeViewUsersQuery(String userId) {
//        return QTask.task.negativeViewUsers.contains(userId);
//    }


    private Predicate buildSingleQuery(TaskSearchRequest request, String actorId, Locale locale) {
        BooleanBuilder builder = new BooleanBuilder();

        buildStringIdQuery(request, builder);
        buildStateQuery(request, builder);
//        buildRoleQuery(request, builder);
        buildCaseQuery(request, builder);
        buildTitleQuery(request, builder);
        buildAssigneeQuery(request, builder);
        buildProcessQuery(request, builder);
        buildFullTextQuery(request, builder);
        buildTransitionQuery(request, builder);
        buildPropertiesQuery(request, builder);

        return builder;
    }

    private void buildStringIdQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.stringId == null || request.stringId.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.stringId.stream().map(this::stringIdQuery).collect(Collectors.toList()),
                        BooleanBuilder::or)
        );
    }

    private void buildStateQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.state == null || request.state.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.state.stream().map(this::stateQuery).collect(Collectors.toList()),
                        BooleanBuilder::or)
        );
    }

    //
//    private void buildRoleQuery(TaskSearchRequest request, BooleanBuilder query) {
//        if (request.role == null || request.role.isEmpty()) {
//            return;
//        }
//
//        query.and(
//                constructPredicateTree(
//                        request.role.stream().map(this::roleQuery).collect(Collectors.toList()),
//                        BooleanBuilder::or)
//        );
//    }
//
//    public Predicate processRoleQuery(String roleId) {
//        return QTask.task.processRolePermissions.permissions.containsKey(roleId);
//    }
//
//    public Predicate caseRoleQuery(String roleId) {
//        return QTask.task.caseRolePermissions.permissions.containsKey(roleId);
//    }
//
    public Predicate stringIdQuery(String id) {
        return QTask.task.id.eq(new ObjectId(id));
    }

    public Predicate stateQuery(State state) {
        return QTask.task.state.eq(state);
    }

    private void buildCaseQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.useCase == null || request.useCase.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.useCase.stream().map(this::caseRequestQuery).filter(Objects::nonNull).collect(Collectors.toList()),
                        BooleanBuilder::or)
        );
    }

    /**
     * @return Predicate for ID if only ID is present. Predicate for title if only title is present.
     * If both are present an ID predicate is returned. If neither are present null is returned.
     */
    private Predicate caseRequestQuery(TaskSearchCaseRequest caseRequest) {
        if (caseRequest.id != null) {
            return caseIdQuery(caseRequest.id);
        } /*else if (caseRequest.title != null) {
            return caseTitleQuery(caseRequest.title);
        }*/
        return null;
    }

    public Predicate caseIdQuery(String caseId) {
        return QTask.task.caseId.eq(caseId);
    }

//    public Predicate caseTitleQuery(String caseTitle) {
//        return QTask.task.containsIgnoreCase(caseTitle);
//    }

    private void buildTitleQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.title == null || request.title.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.title.stream().map(this::titleQuery).collect(Collectors.toList()),
                        BooleanBuilder::or)
        );
    }

    public Predicate titleQuery(String query) {
        return QTask.task.title.defaultValue.containsIgnoreCase(query);
    }

    private void buildAssigneeQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.assigneeId == null || request.assigneeId.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.assigneeId.stream().map(this::assigneeQuery).collect(Collectors.toList()),
                        BooleanBuilder::or)
        );
    }

    public Predicate assigneeQuery(String assigneeId) {
        return QTask.task.assigneeId.eq(assigneeId);
    }

    private void buildProcessQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.process == null || request.process.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.process.stream().map(p -> processQuery(p.identifier)).collect(Collectors.toList()),
                        BooleanBuilder::or)
        );
    }

    public Predicate processQuery(String processId) {
        return QTask.task.processId.eq(processId);
    }

    private void buildFullTextQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.fullText == null || request.fullText.isEmpty()) {
            return;
        }

        query.and(fullTextQuery(request.fullText));
    }

    public Predicate fullTextQuery(String searchedText) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.or(QTask.task.title.defaultValue.containsIgnoreCase(searchedText));
//        builder.or(QTask.task.caseTitle.containsIgnoreCase(searchedText));
        return builder;
    }

    private void buildTransitionQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.transitionId == null || request.transitionId.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.transitionId.stream().map(this::transitionQuery).collect(Collectors.toList()),
                        BooleanBuilder::or)
        );
    }

    public Predicate transitionQuery(String transitionId) {
        return QTask.task.transitionId.eq(transitionId);
    }

    private void buildPropertiesQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.properties == null || request.properties.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.properties.entrySet().stream().map(entry -> this.tagQuery(entry.getKey(), entry.getValue())).collect(Collectors.toList()),
                        BooleanBuilder::and)
        );
    }

    public Predicate tagQuery(String key, String value) {
        return QTask.task.properties.get(key).eq(value);
    }
}