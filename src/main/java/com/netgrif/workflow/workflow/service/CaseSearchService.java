package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.importer.service.FieldFactory;
import com.netgrif.workflow.petrinet.domain.I18nString;
import com.netgrif.workflow.petrinet.domain.PetriNet;
import com.netgrif.workflow.petrinet.domain.QI18nString;
import com.netgrif.workflow.petrinet.domain.dataset.FieldType;
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.workflow.petrinet.web.responsebodies.PetriNetReference;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.QCase;
import com.netgrif.workflow.workflow.domain.QTaskPair;
import com.netgrif.workflow.workflow.domain.TaskPair;
import com.netgrif.workflow.workflow.domain.repositories.TaskRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.Ops;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.SimplePath;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CaseSearchService extends MongoSearchService<Case> {

    private static final Logger log = Logger.getLogger(CaseSearchService.class.getName());

    public static final String PETRINET_IDENTIFIER = "identifier";
    public static final String PETRINET_ID = "id";
    public static final String PETRINET = "petriNet";
    public static final String AUTHOR = "author";
    public static final String AUTHOR_ID = "id";
    public static final String AUTHOR_EMAIL = "email";
    public static final String AUTHOR_NAME = "name";
    public static final String TRANSITION = "transition";
    public static final String FULLTEXT = "fullText";


    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private IPetriNetService petriNetService;

    // **************************
    // * Query building methods *
    // **************************

    public String authorQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(Long.class, o -> ((Long) o).toString());
        builder.put(Integer.class, o -> ((Integer) o).toString());
        builder.put(ArrayList.class, o -> in((List<Object>) obj, oo -> oo.toString(), ob -> ob instanceof Long || ob instanceof Integer));
        builder.put(String.class, o -> {
            Long id = resolveAuthorByEmail((String) obj);
            return id != null ? id.toString() : "";
        });

        return buildQueryPart("author.id", obj, builder);
    }

    public String titleQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> in(((List<Object>) obj), ob -> "\"" + ob + "\"", null));
        builder.put(String.class, o -> regex((String) o, "i"));

        return buildQueryPart("title", obj, builder);
    }

    public String petriNetQuery(Object obj) {
        if (obj instanceof Map) {
            Map<String, Object> q = (Map<String, Object>) obj;
            if (q.containsKey("id"))
                return petriNetIdQuery(q.get("id"));
            else if (q.containsKey("identifier"))
                return petriNetIdentifierQuery(q.get("identifier"));
        }
        return buildQueryPart("petriNet", null, null);
    }

    public String petriNetIdQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> ref("petriNet", obj));
        builder.put(ArrayList.class, o -> in(((List<Object>) obj), oo -> ref("petriNet", oo), null));

        return buildQueryPart("petriNet", obj, builder);
    }

    public String petriNetIdentifierQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> in(((List<Object>) obj), ob -> "\"" + ob + "\"", null));
        builder.put(String.class, o -> "\"" + o + "\"");

        return buildQueryPart("processIdentifier", obj, builder);
    }

    public String dataQuery(Object obj) throws IllegalQueryException {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> {
            StringBuilder strBuilder = new StringBuilder();
            ((List<Object>) o).forEach(dataObj -> {
                strBuilder.append(buildDataSetQuery(dataObj));
                strBuilder.append(",");
            });
            strBuilder.deleteCharAt(strBuilder.length() - 1);
            return strBuilder.toString();
        });
        builder.put(LinkedHashSet.class, CaseSearchService::buildDataSetQuery);

        return buildQueryPart(null, obj, builder);
    }

    private static String buildDataSetQuery(Object o) {
        LinkedHashMap<String, Object> dataObj = (LinkedHashMap<String, Object>) o;
        if (!dataObj.containsKey("id") || !dataObj.containsKey("type") || !dataObj.containsKey("value")) return "";
        return "\"dataSet." + dataObj.get("id") + ".value\":" + resolveDataValue(dataObj.get("value"), (String) dataObj.get("type"));
    }

    public String transitionQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o -> elemMatch(o, oo -> "{\"transition\":\"" + oo + "\"}"));
        builder.put(ArrayList.class, o -> elemMatch(o, oo -> "{\"transition\":" + in(((List<Object>) oo), ob -> "\"" + ob + "\"", null) + "}"));
        builder.put(HashMap.class, o -> {
            Map<String, Object> inner = (Map<String, Object>) o;
            if (inner.get("values") == null)
                return "";
            if (inner.get("combination") != null && ((Boolean) inner.get("combination")))
                return elemMatch(inner.get("values"), oo -> "{\"transition\":" + all(((List<Object>) oo), ob -> "\"" + ob + "\"") + "}");
            else
                return elemMatch(inner.get("values"), oo -> "{\"transition\":" + in(((List<Object>) oo), ob -> "\"" + ob + "\"", null) + "}");
        });

        return buildQueryPart("tasks", obj, builder);
    }


    // **********************
    // *      QueryDSL      *
    // **********************

    public Predicate petriNet(Object query, LoggedUser user, Locale locale) {
        List<PetriNetReference> allowedNets = petriNetService.getReferencesByUsersProcessRoles(user, locale);
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            List<BooleanExpression> expressions = (List<BooleanExpression>) ((ArrayList) query).parallelStream().filter(q -> q instanceof HashMap).map(q -> petriNetObject((HashMap<String, String>) q, allowedNets)).collect(Collectors.toList());
            expressions.forEach(builder::or);
            return builder;
        } else if (query instanceof HashMap) {
            return petriNetObject((HashMap<String, String>) query, allowedNets);
        }
        return Expressions.FALSE;
    }

    private static BooleanExpression petriNetObject(HashMap<String, String> query, List<PetriNetReference> allowedNets) {
        if (query.containsKey(PETRINET_IDENTIFIER) && allowedNets.parallelStream().anyMatch(net -> net.getIdentifier().equalsIgnoreCase(query.get(PETRINET_IDENTIFIER))))
            return QCase.case$.processIdentifier.equalsIgnoreCase(query.get(PETRINET_IDENTIFIER));
        return Expressions.FALSE;
//        else if(query.containsKey(PETRINET_ID) && allowedNets.parallelStream().anyMatch(net->net.getStringId().equalsIgnoreCase(query.get(PETRINET_ID))))
//            return QCase.case$.petriNet._id.e
    }

    public Predicate author(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            List<BooleanExpression> expressions = (List<BooleanExpression>) ((ArrayList) query).parallelStream().filter(q -> q instanceof HashMap).map(q -> authorObject((HashMap<String, Object>) q)).collect(Collectors.toList());
            expressions.forEach(builder::or);
            return builder;
        } else if (query instanceof HashMap) {
            return authorObject((HashMap<String, Object>) query);
        }
        return Expressions.FALSE;
    }

    private static BooleanExpression authorObject(HashMap<String, Object> query) {
        if (query.containsKey(AUTHOR_EMAIL))
            return QCase.case$.author.email.equalsIgnoreCase((String) query.get(AUTHOR_EMAIL));
        else if (query.containsKey(AUTHOR_NAME))
            return QCase.case$.author.fullName.equalsIgnoreCase((String) query.get(AUTHOR_NAME));
        else if (query.containsKey(AUTHOR_ID)) {
            Long searchValue = -1L;
            if (query.get(AUTHOR_ID) instanceof Long)
                searchValue = (Long) query.get(AUTHOR_ID);
            else if (query.get(AUTHOR_ID) instanceof Integer)
                searchValue = ((Integer) query.get(AUTHOR_ID)).longValue();
            return QCase.case$.author.id.eq(searchValue);
        }
        return Expressions.FALSE;
    }

    public Predicate transition(Object query) {
        if (query instanceof ArrayList) {
            BooleanBuilder builder = new BooleanBuilder();
            List<BooleanExpression> expressions = (List<BooleanExpression>) ((ArrayList) query).parallelStream().filter(q -> q instanceof String).map(q -> transitionString((String) q)).collect(Collectors.toList());
            expressions.forEach(builder::or);
            return builder;
        } else if (query instanceof String) {
            return transitionString((String) query);
        }
        return Expressions.FALSE;
    }

    private static BooleanExpression transitionString(String transition) {
        return QCase.case$.tasks.any().transition.eq(transition);
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

        if(processes.isEmpty())
            return Expressions.FALSE;

        List<PetriNet> petriNets = processes.parallelStream().map(process -> petriNetService.getNewestVersionByIdentifier(process)).collect(Collectors.toList());
        if(petriNets.isEmpty())
            return Expressions.FALSE;

        List<BooleanExpression> predicates = new ArrayList<>();
        predicates.add(QCase.case$.visualId.containsIgnoreCase(searchPhrase));
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
                    } else if(field.getType() == FieldType.ENUMERATION) {
                        Path valuePath = Expressions.simplePath(I18nString.class, QCase.case$.dataSet.get(field.getStringId()),"value");
                        Path defaultValuePath = Expressions.stringPath(valuePath,"defaultValue");
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
}
