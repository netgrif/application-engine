package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
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

    public static final String TITLE = "title";
    public static final String ID = "id";
    public static final String ROLE = "role";
    public static final String USER = "user";
    public static final String PROCESS = "process";
    public static final String CASE = "case";
    public static final String TRANSITION = "transition";
    public static final String FULL_TEXT = "fullText";

    public Predicate buildQuery(Map<String, Object> request, LoggedUser user, Locale locale) {
        BooleanBuilder builder = new BooleanBuilder();

        if (request.containsKey(ROLE))
            builder.and(role(request.get(ROLE)));

        if (request.containsKey(CASE))
            builder.and(useCase(request.get(CASE)));

        if (request.containsKey(TITLE))
            builder.and(title(request.get(TITLE)));

        if (request.containsKey(USER))
            builder.and(user(request.get(USER)));

        if (request.containsKey(TRANSITION))
            builder.and(transition(request.get(TRANSITION)));

        if (request.containsKey(PROCESS))
            builder.and(process(request.get(PROCESS)));

        if (request.containsKey(FULL_TEXT))
            builder.and(fullText((String) request.get(FULL_TEXT)));

        return builder;
    }

    public Predicate role(Object query) {
        if (query instanceof ArrayList)
            return constructPredicateTree(((ArrayList<String>) query).stream().map(this::roleString).collect(Collectors.toList()), TaskSearchService::or);
        else if (query instanceof String)
            return roleString((String) query);

        return null;
    }

    private Predicate roleString(String role) {
        return QTask.task.roles.containsKey(role);
    }

    public Predicate useCase(Object query) {
        if (query instanceof HashMap) {
            return caseObject((HashMap<String, Object>) query);
        } else if (query instanceof ArrayList) {
            return caseArray((ArrayList<String>) query, ID);
        } else if (query instanceof String) {
            return caseId((String) query);
        }

        return null;
    }

    private Predicate caseObject(Map<String, Object> query) {
        if (query.containsKey(TITLE)) {
            return query.get(TITLE) instanceof ArrayList ? caseArray((ArrayList<String>) query.get(TITLE), TITLE) : caseTitle((String) query.get(TITLE));
        } else if (query.containsKey(ID)) {
            return query.get(ID) instanceof ArrayList ? caseArray((ArrayList<String>) query.get(ID), ID) : caseId((String) query.get(ID));
        }

        return null;
    }

    private Predicate caseArray(ArrayList<String> query, String key) {
        return constructPredicateTree(query.stream().map(q -> {
            if (key.equalsIgnoreCase(TITLE))
                return caseTitle(q);
            else
                return caseId(q);
        }).collect(Collectors.toList()), TaskSearchService::or);
    }

    private Predicate caseId(String caseId) {
        return QTask.task.caseId.eq(caseId);
    }

    private Predicate caseTitle(String caseTitle) {
        return QTask.task.caseTitle.containsIgnoreCase(caseTitle);
    }

    public Predicate title(Object query) {
        if (query instanceof ArrayList)
            return constructPredicateTree(((ArrayList<String>) query).stream().map(this::titleString).collect(Collectors.toList()), TaskSearchService::or);
        else if (query instanceof String)
            return titleString((String) query);

        return null;
    }

    private Predicate titleString(String query) {
        return QTask.task.title.defaultValue.containsIgnoreCase(query);
    }

    public Predicate user(Object query) {
        if (query instanceof ArrayList)
            return constructPredicateTree(((ArrayList<Number>) query).stream().map(this::userLong).collect(Collectors.toList()), TaskSearchService::or);
        else if (query instanceof Integer)
            return userLong(Long.valueOf(((Integer) query).longValue()));
        else if (query instanceof Long)
            return userLong((Long) query);
        else if (query instanceof String) {
            try {
                return userLong(Long.parseLong((String) query));
            } catch (NumberFormatException queryShouldBeEmail) {
                return userString((String) query);
            }
        }

        return null;
    }

    private Predicate userLong(Number userId) {
        if (userId instanceof Integer)
            return QTask.task.userId.eq(Long.valueOf(((Integer) userId).longValue()));
        else if (userId instanceof Long)
            return QTask.task.userId.eq((Long) userId);
        return null;
    }

    private Predicate userString(String userEmail) {
        Long id = resolveAuthorByEmail(userEmail);
        if (id != null)
            return userLong(id);
        return null;
    }

    public Predicate transition(Object query) {
        if (query instanceof ArrayList)
            return constructPredicateTree(((ArrayList<String>) query).stream().map(this::transitionString).collect(Collectors.toList()), TaskSearchService::or);
        else if (query instanceof String)
            return transitionString((String) query);

        return null;
    }

    private Predicate transitionString(String transitionId) {
        return QTask.task.transitionId.eq(transitionId);
    }

    public Predicate process(Object query) {
        if (query instanceof ArrayList)
            return constructPredicateTree(((ArrayList<String>) query).stream().map(this::processString).collect(Collectors.toList()), TaskSearchService::or);
        else if (query instanceof String)
            return processString((String) query);

        return null;
    }

    private Predicate processString(String processId) {
        return QTask.task.processId.eq(processId);
    }

    public Predicate fullText(String query) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.or(QTask.task.title.defaultValue.containsIgnoreCase(query));
        builder.or(QTask.task.caseTitle.containsIgnoreCase(query));
        return builder;
    }

    private BooleanBuilder constructPredicateTree(List<Predicate> elementaryPredicates, BiFunction<BooleanBuilder, Predicate, BooleanBuilder> nodeOperation) {
        ArrayDeque<BooleanBuilder> subtrees = new ArrayDeque<>(elementaryPredicates.size()/2 + elementaryPredicates.size()%2);

        for(Iterator<Predicate> predicateIterator = elementaryPredicates.iterator(); predicateIterator.hasNext();) {
            BooleanBuilder subtree = new BooleanBuilder(predicateIterator.next());
            if(predicateIterator.hasNext())
                nodeOperation.apply(subtree, predicateIterator.next());
            subtrees.addFirst(subtree);
        }

        while(subtrees.size()!=1)
            subtrees.addLast(nodeOperation.apply(subtrees.pollFirst(), subtrees.pollFirst()));

        return subtrees.peekFirst();
    }

    private static BooleanBuilder or(BooleanBuilder leftSubtree, Predicate rightSubtree) {
        return leftSubtree.or(rightSubtree);
    }
}