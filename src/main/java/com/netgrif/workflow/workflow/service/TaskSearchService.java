package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.workflow.workflow.domain.QTask;
import com.netgrif.workflow.workflow.domain.Task;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Service
public class TaskSearchService extends MongoSearchService<Task> {

    public Predicate buildQuery(List<TaskSearchRequest> requests, LoggedUser user, Boolean isIntersection) {
        List<Predicate> singleQueries = requests.stream().map(this::buildSingleQuery).collect(Collectors.toList());

        BooleanBuilder builder = constructPredicateTree(singleQueries, isIntersection ? BooleanBuilder::and : BooleanBuilder::or);

        builder.and(buildRolesQueryConstraint(user));

        return builder;
    }

    protected Predicate buildRolesQueryConstraint(LoggedUser user) {
        List<Predicate> roleConstraints = user.getProcessRoles().stream().map(this::roleQuery).collect(Collectors.toList());
        return constructPredicateTree(roleConstraints, BooleanBuilder::or);
    }

    private Predicate buildSingleQuery(TaskSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();

        buildRoleQuery(request, builder);
        buildCaseQuery(request, builder);
        buildTitleQuery(request, builder);
        buildUserQuery(request, builder);
        buildProcessQuery(request, builder);
        buildFullTextQuery(request, builder);
        buildTransitionQuery(request, builder);

        return builder;
    }

    private void buildRoleQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.role == null || request.role.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.role.stream().map(this::roleQuery).collect(Collectors.toList()),
                        BooleanBuilder::or)
        );
    }

    public Predicate roleQuery(String role) {
        return QTask.task.roles.containsKey(role);
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
    private Predicate caseRequestQuery(TaskSearchRequest.TaskSearchCaseRequest caseRequest) {
        if (caseRequest.id != null) {
            return caseIdQuery(caseRequest.id);
        } else if (caseRequest.title != null) {
            return caseTitleQuery(caseRequest.title);
        }
        return null;
    }

    public Predicate caseIdQuery(String caseId) {
        return QTask.task.caseId.eq(caseId);
    }

    public Predicate caseTitleQuery(String caseTitle) {
        return QTask.task.caseTitle.containsIgnoreCase(caseTitle);
    }

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

    private void buildUserQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.user == null || request.user.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.user.stream().map(this::userQuery).collect(Collectors.toList()),
                        BooleanBuilder::or)
        );
    }

    public Predicate userQuery(Long userId) {
        return QTask.task.userId.eq(userId);
    }

    private void buildProcessQuery(TaskSearchRequest request, BooleanBuilder query) {
        if (request.process == null || request.process.isEmpty()) {
            return;
        }

        query.and(
                constructPredicateTree(
                        request.process.stream().map(this::processQuery).collect(Collectors.toList()),
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
        builder.or(QTask.task.caseTitle.containsIgnoreCase(searchedText));
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

    private BooleanBuilder constructPredicateTree(List<Predicate> elementaryPredicates, BiFunction<BooleanBuilder, Predicate, BooleanBuilder> nodeOperation) {
        if (elementaryPredicates.size() == 0)
            return new BooleanBuilder();

        ArrayDeque<BooleanBuilder> subtrees = new ArrayDeque<>(elementaryPredicates.size() / 2 + elementaryPredicates.size() % 2);

        for (Iterator<Predicate> predicateIterator = elementaryPredicates.iterator(); predicateIterator.hasNext(); ) {
            BooleanBuilder subtree = new BooleanBuilder(predicateIterator.next());
            if (predicateIterator.hasNext())
                nodeOperation.apply(subtree, predicateIterator.next());
            subtrees.addFirst(subtree);
        }

        while (subtrees.size() != 1)
            subtrees.addLast(nodeOperation.apply(subtrees.pollFirst(), subtrees.pollFirst()));

        return subtrees.peekFirst();
    }
}