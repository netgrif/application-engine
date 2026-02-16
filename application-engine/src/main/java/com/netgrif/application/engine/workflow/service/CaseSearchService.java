package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.adapter.spring.workflow.domain.QTask;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNet;
import com.netgrif.application.engine.objects.petrinet.domain.PetriNetSearch;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.ActorFieldValue;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.ProcessResourceId;
import com.netgrif.application.engine.adapter.spring.workflow.domain.QCase;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class CaseSearchService extends MongoSearchService<Case> {

    private static final Logger log = LoggerFactory.getLogger(CaseSearchService.class.getName());

    public static final String ROLE = "role";
    public static final String DATA = "data";
    public static final String TAGS = "tags";
    public static final String PETRINET_IDENTIFIER = "identifier";
    public static final String PETRINET_ID = "id";
    public static final String PETRINET = "petriNet";

    public static final String AUTHOR = "author";
    public static final String AUTHOR_ID = "id";
    public static final String AUTHOR_DISPLAYNAME = "displayName";
    public static final String AUTHOR_IDENTIFIER = "identifier";
    public static final String AUTHOR_REALM = "realmId";
    public static final String AUTHOR_NAME = "name";
    public static final String AUTHOR_USERNAME = "username";

    public static final String TRANSITION = "transition";
    public static final String FULLTEXT = "fullText";
    public static final String CASE_ID = "stringId";
    public static final String GROUP = "group";

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private UserService userService;

    public Predicate buildQuery(Map<String, Object> requestQuery, Locale locale) {
        BooleanBuilder builder = new BooleanBuilder();
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(userService.getLoggedOrSystem());
//        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();

        if (requestQuery.containsKey(PETRINET)) {
            builder.and(petriNet(requestQuery.get(PETRINET), locale));
        }
        if (requestQuery.containsKey(AUTHOR)) {
            builder.and(author(requestQuery.get(AUTHOR)));
        }
        if (requestQuery.containsKey(TRANSITION)) {
            builder.and(transition(requestQuery.get(TRANSITION)));
        }
        if (requestQuery.containsKey(FULLTEXT)) {
            builder.and(fullText(requestQuery.getOrDefault(PETRINET, null), (String) requestQuery.get(FULLTEXT)));
        }
        if (requestQuery.containsKey(ROLE)) {
            builder.and(role(requestQuery.get(ROLE)));
        }
        if (requestQuery.containsKey(DATA)) {
            builder.and(data(requestQuery.get(DATA)));
        }
        if (requestQuery.containsKey(TAGS)) {
            builder.and(tags(requestQuery.get(TAGS)));
        }
        if (requestQuery.containsKey(CASE_ID)) {
            builder.and(caseId(requestQuery.get(CASE_ID)));
        }
        if (requestQuery.containsKey(GROUP)) {
            Predicate groupPredicate = group(requestQuery.get(GROUP), locale);
            if (groupPredicate != null) {
                builder.and(groupPredicate);
            } else {
                return null;
            }
        }
        BooleanBuilder permissionConstraints = new BooleanBuilder(buildViewRoleQueryConstraint(loggedUser));
        permissionConstraints.andNot(buildNegativeViewRoleQueryConstraint(loggedUser));
        permissionConstraints.or(buildViewActorQueryConstraint(loggedUser));
        permissionConstraints.andNot(buildNegativeViewActorsQueryConstraint(loggedUser));
        if (!loggedUser.isAdmin()) {
            // todo 2072 test
            permissionConstraints.and(buildWorkspaceQueryConstraint(loggedUser));
        }
        builder.and(permissionConstraints);

        return builder;
    }

    protected Predicate buildViewRoleQueryConstraint(LoggedUser user) {
        List<Predicate> roleConstraints = user.getProcessRoles().stream().map(r -> viewRoleQuery(r.getStringId())).collect(Collectors.toList());
        return constructPredicateTree(roleConstraints, BooleanBuilder::or);
    }

    public Predicate viewRoleQuery(String role) {
        return QCase.case$.viewActorRefs.isEmpty().and(QCase.case$.viewRoles.isEmpty()).or(QCase.case$.viewRoles.contains(role));
    }

    protected Predicate buildViewActorQueryConstraint(LoggedUser user) {
        List<Predicate> userConstraints = getActorIdsOfUser(user).stream().map(this::viewActorQuery).toList();
        return constructPredicateTree(userConstraints, BooleanBuilder::or);
    }

    public Predicate viewActorQuery(String actorId) {
        return QCase.case$.viewActorRefs.isEmpty().and(QCase.case$.viewRoles.isEmpty()).or(QCase.case$.viewActors.contains(actorId));
    }

    protected Predicate buildNegativeViewRoleQueryConstraint(LoggedUser user) {
        List<Predicate> roleConstraints = user.getProcessRoles().stream().map(r -> negativeViewRoleQuery(r.getStringId())).collect(Collectors.toList());
        return constructPredicateTree(roleConstraints, BooleanBuilder::or);
    }

    public Predicate negativeViewRoleQuery(String role) {
        return QCase.case$.negativeViewRoles.contains(role);
    }

    protected Predicate buildNegativeViewActorsQueryConstraint(LoggedUser user) {
        List<Predicate> userConstraints = getActorIdsOfUser(user).stream().map(this::negativeViewActorQuery).toList();
        return constructPredicateTree(userConstraints, BooleanBuilder::or);
    }

    public Predicate negativeViewActorQuery(String actorId) {
        return QCase.case$.negativeViewActors.contains(actorId);
    }

    public Predicate petriNet(Object query, Locale locale) {
        List<PetriNetReference> allowedNets = petriNetService.getReferencesByUsersProcessRoles(locale);
        if (query instanceof ArrayList) {
            List<Predicate> expressions = (List<Predicate>) ((ArrayList) query).stream().filter(q -> q instanceof HashMap).map(q -> petriNetObject((HashMap<String, String>) q, allowedNets)).collect(Collectors.toList());
            return constructPredicateTree(expressions, BooleanBuilder::or);
        } else if (query instanceof HashMap) {
            return petriNetObject((HashMap<String, String>) query, allowedNets);
        }
        return null;
    }

    private static BooleanExpression petriNetObject(HashMap<String, String> query, List<PetriNetReference> allowedNets) {
        if (query.containsKey(PETRINET_IDENTIFIER) && allowedNets.stream().anyMatch(net -> net.getIdentifier().equalsIgnoreCase(query.get(PETRINET_IDENTIFIER))))
            return QCase.case$.processIdentifier.equalsIgnoreCase(query.get(PETRINET_IDENTIFIER));
        return null;
    }

    public Predicate author(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            List<BooleanExpression> expressions = (List<BooleanExpression>) ((ArrayList) query).stream().filter(q -> q instanceof HashMap).map(q -> authorObject((HashMap<String, Object>) q)).collect(Collectors.toList());
            expressions.forEach(builder::or);
            return builder;
        } else if (query instanceof HashMap) {
            return authorObject((HashMap<String, Object>) query);
        }
        return null;
    }

    public Predicate role(Object o) {
        if (o instanceof ArrayList) {
            return QCase.case$.enabledRoles.any().in((ArrayList<String>) o);
        }
        return null;
    }

    private static BooleanExpression authorObject(HashMap<String, Object> query) {
        if (query.containsKey(AUTHOR_NAME)) {
            return QCase.case$.author.displayName.equalsIgnoreCase((String) query.get(AUTHOR_NAME));
        } else if (query.containsKey(AUTHOR_ID)) {
            String searchValue = "";
            if (query.get(AUTHOR_ID) instanceof String)
                searchValue = (String) query.get(AUTHOR_ID);
            return QCase.case$.author.id.eq(searchValue);
        } else if (query.containsKey(AUTHOR_DISPLAYNAME)) {
            return QCase.case$.author.displayName.equalsIgnoreCase((String) query.get(AUTHOR_DISPLAYNAME));
        } else if (query.containsKey(AUTHOR_IDENTIFIER)) {
            return QCase.case$.author.identifier.equalsIgnoreCase((String) query.get(AUTHOR_IDENTIFIER));
        } else if (query.containsKey(AUTHOR_USERNAME)) {
            return QCase.case$.author.identifier.equalsIgnoreCase((String) query.get(AUTHOR_USERNAME));
        } else if (query.containsKey(AUTHOR_REALM)) {
            return QCase.case$.author.realmId.equalsIgnoreCase((String) query.get(AUTHOR_REALM));
        }
        return null;
    }

    public Predicate transition(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            List<BooleanExpression> expressions = (List<BooleanExpression>) ((ArrayList) query).stream().filter(q -> q instanceof String).map(q -> transitionString((String) q)).collect(Collectors.toList());
            expressions.forEach(builder::or);
            return builder;
        } else if (query instanceof String) {
            return transitionString((String) query);
        }
        return null;
    }

    private static BooleanExpression transitionString(String transition) {
        return QCase.case$.tasks.any().transition.eq(transition);
    }

    public Predicate data(Object data) {
        if (!(data instanceof Map)) {
            throw new IllegalArgumentException("Unsupported class " + data.getClass().getName());
        }
        Map dataQueries = (Map) data;

        List<BooleanExpression> predicates = new ArrayList<>();
        (dataQueries).forEach((k, v) -> {
            if (v instanceof Map) {
                Map.Entry<String, Object> entry = (Map.Entry<String, Object>) ((Map) v).entrySet().iterator().next();
                Object fieldValue = entry.getValue();
                try {
                    FieldType type = FieldType.fromString(entry.getKey());

                    switch (type) {
                        case ACTOR:
                            Path valuePath = Expressions.simplePath(ActorFieldValue.class, QCase.case$.dataSet.get((String) k), "value");
                            Path idPath = Expressions.stringPath(valuePath, "id");
                            Expression<Long> constant = Expressions.constant(Long.valueOf("" + fieldValue));
                            predicates.add(Expressions.predicate(Ops.EQ, idPath, constant));
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Unrecognized Field type {}", entry.getKey());
                }
            } else {
                predicates.add(QCase.case$.dataSet.get((String) k).value.eq(v));
            }
        });
        BooleanBuilder builder = new BooleanBuilder();
        predicates.forEach(builder::and);
        return builder;
    }

    public Predicate tags(Object tags) {
        if (!(tags instanceof Map)) {
            throw new IllegalArgumentException("Unsupported class " + tags.getClass().getName());
        }
        Map tagsQueries = (Map) tags;

        List<BooleanExpression> predicates = new ArrayList<>();
        (tagsQueries).forEach((k, v) -> {
            if (k instanceof String && v instanceof String) {
                predicates.add(QCase.case$.tags.get((String) k).eq((String) v));
            } else {
                throw new IllegalArgumentException("Unsupported class in key or value tags element (" + k.getClass().getName() + "," + v.getClass().getName() + ")");
            }
        });
        BooleanBuilder builder = new BooleanBuilder();
        predicates.forEach(builder::and);
        return builder;
    }

    public Predicate fullText(Object petriNetQuery, String searchPhrase) {
        List<String> processes = new ArrayList<>();
        if (petriNetQuery instanceof ArrayList) {
            ((ArrayList) petriNetQuery).forEach(net -> {
                if (net instanceof HashMap)
                    processes.add(parseProcessIdentifier((HashMap) net));
            });
        } else if (petriNetQuery instanceof HashMap) {
            processes.add(parseProcessIdentifier((HashMap) petriNetQuery));
        }

        List<PetriNet> petriNets;
        if (processes.isEmpty()) {
            // TODO JOFO: unpaged necessary?
            petriNets = petriNetService.getAll(Pageable.unpaged()).getContent();
        } else {
            petriNets = processes.stream().map(process -> petriNetService.getDefaultVersionByIdentifier(process)).collect(Collectors.toList());
        }
        if (petriNets.isEmpty())
            return null;

        List<BooleanExpression> predicates = new ArrayList<>();
        predicates.add(QCase.case$.visualId.startsWithIgnoreCase(searchPhrase));
        predicates.add(QCase.case$.title.containsIgnoreCase(searchPhrase));
        predicates.add(QCase.case$.author.displayName.containsIgnoreCase(searchPhrase));
        predicates.add(QCase.case$.author.identifier.containsIgnoreCase(searchPhrase));

        try {
            LocalDateTime creation = FieldFactory.parseDateTime(searchPhrase);
            if (creation != null)
                predicates.add(QCase.case$.creationDate.eq(creation));
        } catch (Exception e) {
            //ignore
        }

        petriNets.forEach(net -> {
            net.getImmediateFields().forEach(field -> {
                try {
                    if (field.getType() == FieldType.TEXT) {
                        Path<?> path = QCase.case$.dataSet.get(field.getStringId()).value;
                        Expression<String> constant = Expressions.constant(searchPhrase);
                        predicates.add(Expressions.predicate(Ops.STRING_CONTAINS_IC, path, constant));
                    } else if (field.getType() == FieldType.NUMBER) {
                        Double value = FieldFactory.parseDouble(searchPhrase);
                        if (value != null)
                            predicates.add(QCase.case$.dataSet.get(field.getStringId()).value.eq(value));
                    } else if (field.getType() == FieldType.DATE) {
                        LocalDate value = FieldFactory.parseDate(searchPhrase);
                        if (value != null)
                            predicates.add(QCase.case$.dataSet.get(field.getStringId()).value.eq(value));
                    } else if (field.getType() == FieldType.DATETIME) {
                        LocalDateTime value = FieldFactory.parseDateTime(searchPhrase);
                        if (value != null)
                            predicates.add(QCase.case$.dataSet.get(field.getStringId()).value.eq(value));
                    } else if (field.getType() == FieldType.ENUMERATION) {
                        Path valuePath = Expressions.simplePath(I18nString.class, QCase.case$.dataSet.get(field.getStringId()), "value");
                        Path defaultValuePath = Expressions.stringPath(valuePath, "defaultValue");
                        Expression<String> constant = Expressions.constant(searchPhrase);
                        predicates.add(Expressions.predicate(Ops.STRING_CONTAINS_IC, defaultValuePath, constant));
                    }
                } catch (Exception e) {
                    log.error(e.getMessage());
                    //Skip this field in search
                }
            });
        });

        BooleanBuilder builder = new BooleanBuilder();
        predicates.forEach(builder::or);
        return builder;
    }

    private String parseProcessIdentifier(HashMap<String, String> petriNet) {
        if (petriNet.containsKey(PETRINET_IDENTIFIER))
            return petriNet.get(PETRINET_IDENTIFIER);
        return "";
    }

    public Predicate caseId(Object query) {
        if (query instanceof ArrayList) {
            List<Predicate> expressions = (List<Predicate>) ((ArrayList) query).stream().filter(q -> q instanceof String).map(q -> caseIdString((String) q)).collect(Collectors.toList());
            return constructPredicateTree(expressions, BooleanBuilder::or);
        } else if (query instanceof String) {
            return caseIdString((String) query);
        }
        return null;
    }

    protected Predicate buildWorkspaceQueryConstraint(LoggedUser user) {
        return QTask.task.workspaceId.eq(user.getActiveWorkspaceId());
    }

    private static BooleanExpression caseIdString(String caseId) {
        return caseId.isEmpty() ? QCase.case$._id.isNull() : QCase.case$._id.eq(new ProcessResourceId(caseId));
    }

    public Predicate group(Object query, Locale locale) {
        PetriNetSearch processQuery = new PetriNetSearch();
        if (query instanceof List) {
            processQuery.setGroup((List<String>) query);
        } else if (query instanceof String) {
            processQuery.setGroup(new ArrayList<>(Arrays.asList((String) query)));
        }
        List<PetriNetReference> groupProcesses = this.petriNetService.search(processQuery, new FullPageRequest(), locale).getContent();
        if (groupProcesses.isEmpty())
            return null;

        List<Predicate> processQueries = groupProcesses.stream().map(PetriNetReference::getIdentifier).map(QCase.case$.processIdentifier::eq).collect(Collectors.toList());
        return constructPredicateTree(processQueries, BooleanBuilder::or);
    }
}
