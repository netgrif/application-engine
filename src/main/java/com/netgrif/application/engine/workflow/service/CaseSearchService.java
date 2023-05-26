package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.importer.service.FieldFactory;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.application.engine.utils.FullPageRequest;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    public static final String PETRINET_IDENTIFIER = "identifier";
    public static final String PETRINET_ID = "id";
    public static final String PETRINET = "petriNet";
    public static final String AUTHOR = "author";
    public static final String AUTHOR_ID = "id";
    public static final String AUTHOR_EMAIL = "email";
    public static final String AUTHOR_NAME = "name";
    public static final String TRANSITION = "transition";
    public static final String FULLTEXT = "fullText";
    public static final String CASE_ID = "stringId";
    public static final String GROUP = "group";

    @Autowired
    private IPetriNetService petriNetService;

    public Predicate buildQuery(Map<String, Object> requestQuery, LoggedUser user, Locale locale) {
        BooleanBuilder builder = new BooleanBuilder();
        LoggedUser loggedOrImpersonated = user.getSelfOrImpersonated();

        if (requestQuery.containsKey(PETRINET)) {
            builder.and(petriNet(requestQuery.get(PETRINET), loggedOrImpersonated, locale));
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
        if (requestQuery.containsKey(CASE_ID)) {
            builder.and(caseId(requestQuery.get(CASE_ID)));
        }
        if (requestQuery.containsKey(GROUP)) {
            Predicate groupPredicate = group(requestQuery.get(GROUP), loggedOrImpersonated, locale);
            if (groupPredicate != null) {
                builder.and(groupPredicate);
            } else {
                return null;
            }
        }
        BooleanBuilder permissionConstraints = new BooleanBuilder(buildViewRoleQueryConstraint(loggedOrImpersonated));
        permissionConstraints.andNot(buildNegativeViewRoleQueryConstraint(loggedOrImpersonated));
        permissionConstraints.or(buildViewUserQueryConstraint(loggedOrImpersonated));
        permissionConstraints.andNot(buildNegativeViewUsersQueryConstraint(loggedOrImpersonated));
        builder.and(permissionConstraints);
        return builder;
    }

    protected Predicate buildViewRoleQueryConstraint(LoggedUser user) {
        List<Predicate> roleConstraints = user.getProcessRoles().stream().map(this::viewRoleQuery).collect(Collectors.toList());
        return constructPredicateTree(roleConstraints, BooleanBuilder::or);
    }

    public Predicate viewRoleQuery(String role) {
        return QCase.case$.viewUserRefs.isEmpty().and(QCase.case$.viewRoles.isEmpty()).or(QCase.case$.viewRoles.contains(role));
    }

    protected Predicate buildViewUserQueryConstraint(LoggedUser user) {
        Predicate roleConstraints = viewUserQuery(user.getId());
        return constructPredicateTree(Collections.singletonList(roleConstraints), BooleanBuilder::or);
    }

    public Predicate viewUserQuery(String userId) {
        return QCase.case$.viewUserRefs.isEmpty().and(QCase.case$.viewRoles.isEmpty()).or(QCase.case$.viewUsers.contains(userId));
    }

    protected Predicate buildNegativeViewRoleQueryConstraint(LoggedUser user) {
        List<Predicate> roleConstraints = user.getProcessRoles().stream().map(this::negativeViewRoleQuery).collect(Collectors.toList());
        return constructPredicateTree(roleConstraints, BooleanBuilder::or);
    }

    public Predicate negativeViewRoleQuery(String role) {
        return QCase.case$.negativeViewRoles.contains(role);
    }

    protected Predicate buildNegativeViewUsersQueryConstraint(LoggedUser user) {
        Predicate roleConstraints = negativeViewUserQuery(user.getId());
        return constructPredicateTree(Collections.singletonList(roleConstraints), BooleanBuilder::or);
    }

    public Predicate negativeViewUserQuery(String userId) {
        return QCase.case$.negativeViewUsers.contains(userId);
    }

    public Predicate petriNet(Object query, LoggedUser user, Locale locale) {
        List<PetriNetReference> allowedNets = petriNetService.getReferencesByUsersProcessRoles(user, locale);
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            List<BooleanExpression> expressions = (List<BooleanExpression>) ((ArrayList) query).stream().filter(q -> q instanceof HashMap).map(q -> petriNetObject((HashMap<String, String>) q, allowedNets)).collect(Collectors.toList());
            expressions.forEach(builder::or);
            return builder;
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
        if (query.containsKey(AUTHOR_EMAIL))
            return QCase.case$.author.email.equalsIgnoreCase((String) query.get(AUTHOR_EMAIL));
        else if (query.containsKey(AUTHOR_NAME))
            return QCase.case$.author.fullName.equalsIgnoreCase((String) query.get(AUTHOR_NAME));
        else if (query.containsKey(AUTHOR_ID)) {
            String searchValue = "";
            if (query.get(AUTHOR_ID) instanceof String)
                searchValue = (String) query.get(AUTHOR_ID);
            return QCase.case$.author.id.eq(searchValue);
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
                        case USER:
                            Path valuePath = Expressions.simplePath(UserFieldValue.class, QCase.case$.dataSet.get((String) k), "value");
                            Path idPath = Expressions.stringPath(valuePath, "id");
                            Expression<Long> constant = Expressions.constant(Long.valueOf("" + fieldValue));
                            predicates.add(Expressions.predicate(Ops.EQ, idPath, constant));
                            break;
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Unrecognized Field type " + entry.getKey());
                }
            } else {
                predicates.add(QCase.case$.dataSet.get((String) k).value.eq(v));
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
            petriNets = petriNetService.getAll();
        } else {
            petriNets = processes.stream().map(process -> petriNetService.getNewestVersionByIdentifier(process)).collect(Collectors.toList());
        }
        if (petriNets.isEmpty())
            return null;

        List<BooleanExpression> predicates = new ArrayList<>();
        predicates.add(QCase.case$.visualId.startsWithIgnoreCase(searchPhrase));
        predicates.add(QCase.case$.title.containsIgnoreCase(searchPhrase));
        predicates.add(QCase.case$.author.fullName.containsIgnoreCase(searchPhrase));
        predicates.add(QCase.case$.author.email.containsIgnoreCase(searchPhrase));

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
            BooleanBuilder builder = new BooleanBuilder();
            List<BooleanExpression> expressions = (List<BooleanExpression>) ((ArrayList) query).stream().filter(q -> q instanceof String).map(q -> caseIdString((String) q)).collect(Collectors.toList());
            expressions.forEach(builder::or);
            return builder;
        } else if (query instanceof String) {
            return caseIdString((String) query);
        }
        return null;
    }

    private static BooleanExpression caseIdString(String caseId) {
        return QCase.case$._id.eq(new ObjectId(caseId));
    }

    public Predicate group(Object query, LoggedUser user, Locale locale) {
        Map<String, Object> processQuery = new HashMap<>();
        processQuery.put(GROUP, query);
        List<PetriNetReference> groupProcesses = this.petriNetService.search(processQuery, user, new FullPageRequest(), locale).getContent();
        if (groupProcesses.size() == 0)
            return null;

        List<BooleanExpression> processQueries = groupProcesses.stream().map(PetriNetReference::getIdentifier).map(QCase.case$.processIdentifier::eq).collect(Collectors.toList());
        BooleanBuilder builder = new BooleanBuilder();
        processQueries.forEach(builder::or);
        return builder;
    }
}
