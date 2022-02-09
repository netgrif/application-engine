package com.netgrif.application.engine.workflow.service;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.querydsl.core.BooleanBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

@Service
public class MongoSearchService<T> {

    private static final Logger log = LoggerFactory.getLogger(MongoSearchService.class.getName());

    private static final String ERROR_KEY = "ERROR";

    @Autowired
    private IUserService userService;

    @Autowired
    private MongoTemplate mongoTemplate;

    private Class<T> tClass;

    public Page<T> search(Map<String, Object> searchRequest, Pageable pageable, Class<T> clazz) {
        try {
            this.tClass = clazz;
            return executeQuery(buildQuery(resolveRequest(searchRequest)), pageable);
        } catch (IllegalQueryException e) {
            log.error("Searching failed: ", e);
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    protected Map<String, Object> resolveRequest(Map<String, Object> request) {
        Map<String, Object> queryParts = new LinkedHashMap<>();
        boolean match = request.entrySet().stream().allMatch((Map.Entry<String, Object> entry) -> {
            try {
                Method method = this.getClass().getMethod(entry.getKey() + "Query", Object.class);
//                log.info("Resolved attribute of " + tClass.getSimpleName() + ": " + entry.getKey());
                Object part = method.invoke(this, entry.getValue());
                if (part != null) //TODO 23.7.2017 throw exception when cannot build query
                    queryParts.put(entry.getKey(), part);
                return true;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                log.error("Resolving request failed: ", e);
                queryParts.put(ERROR_KEY, "Parameter " + entry.getKey() + " is not supported in " + tClass.getSimpleName() + " search!");
                return false;
            }
        });

        return queryParts;
    }

    public String buildQuery(Map<String, Object> queryParts) throws IllegalQueryException {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean result = queryParts.entrySet().stream().allMatch(entry -> {
            if (entry.getKey().equals(ERROR_KEY)) return false;
            if (((String) entry.getValue()).endsWith(":")) {
                queryParts.put(ERROR_KEY, "Query attribute " + entry.getKey() + " has wrong value " + entry.getValue());
                return false;
            }

//            log.info("Query: " + entry.getValue());
            builder.append(entry.getValue());
            builder.append(",");
            return true;
        });
        if (!result)
            throw new IllegalQueryException((String) (queryParts.get(ERROR_KEY)));
        if (builder.length() > 1)
            builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }

    protected Page<T> executeQuery(String queryString, Pageable pageable) {
        Query query = new BasicQuery(queryString).with(pageable);
        log.info("Executing search query: " + queryString);
        return new PageImpl<>(mongoTemplate.find(query, tClass),
                pageable,
                mongoTemplate.count(new BasicQuery(queryString, "{_id:1}"), tClass));
    }


    // **************************
    // * Query building methods *
    // **************************

    public String idQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> in((List<Object>) obj, ob -> oid((String) ob), null));
        builder.put(String.class, o -> oid((String) o));

        return buildQueryPart("_id", obj, builder);
    }

    public String orQuery(Object obj) throws IllegalQueryException {
        if (!(obj instanceof Map)) throw new IllegalQueryException("Parameter or must have JSON Object as value!");

        Map<String, Object> orMap = resolveRequest((Map<String, Object>) obj);
        return or(orMap.values());
    }

    // ***********************************************
    // * Helper methods for building mongodb queries *
    // ***********************************************

    protected String buildQueryPart(String attribute, Object obj, Map<Class, Function<Object, String>> builder) {
        try {
            if (obj == null || (obj instanceof List && ((List) obj).isEmpty()))
                return ":";

            String attr = attribute != null ? "\"" + attribute + "\":" : "";
            return attr + builder.get(obj.getClass()).apply(obj);
        } catch (NullPointerException e) {
            e.getStackTrace();
            return ":";
        }
    }

    protected String resolveAuthorByEmail(String email) {
        IUser user = userService.findByEmail(email, true);
        return user != null ? user.getStringId() : null;
    }

    public static String oid(String id) {
        return "{$oid:\"" + id + "\"}";
    }

    public static String in(List<Object> values, Function<Object, String> valueQueryBuilder, Predicate<Object> typeTest) {
        StringBuilder builder = new StringBuilder();
        builder.append("{$in:[");
        values.forEach(o -> {
            if (typeTest != null && !typeTest.test(o)) return;

            builder.append(valueQueryBuilder.apply(o));

            builder.append(",");
        });
        if (!values.isEmpty())
            builder.deleteCharAt(builder.length() - 1);
        builder.append("]}");
        return builder.toString();
    }

    public static String all(List<Object> values, Function<Object, String> valueQueryBuilder) {
        StringBuilder builder = new StringBuilder();
        builder.append("{$all:[");
        values.forEach(v -> {
            builder.append(valueQueryBuilder.apply(v));
            builder.append(",");
        });
        if (!values.isEmpty())
            builder.deleteCharAt(builder.length() - 1);
        builder.append("]}");
        return builder.toString();
    }

    public static String ref(String attr, Object id) {
        return "{$ref:\"" + attr + "\",$id:" + oid((String) id) + "}";
    }

    public static String or(Collection<Object> expressions) {
        if (expressions.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder();
        builder.append("$or:[");
        expressions.forEach(obj -> {
            builder.append("{");
            if (obj instanceof String && ((String) obj).equalsIgnoreCase(":"))
                builder.append("");
            else
                builder.append(obj);
            builder.append("},");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]");
        return builder.toString();
    }

    public static String exists(boolean val) {
        return "{$exists:" + val + "}";
    }

    public static String regex(String val, String opt) {
        return "{$regex:'" + val + "',$options:'" + opt + "'}";
    }

    public static String lessThenOrEqual(Object val) {
        return "{$lte:" + val + "}";
    }

    public static String elemMatch(Object obj, Function<Object, String> valueQueryBuilder) {
        return "{$elemMatch:" + valueQueryBuilder.apply(obj) + "}";
    }

    public static Object resolveDataValue(Object val, String type) {
        switch (type) {
            case "string":
                return "\"" + val + "\"";
            case "date":
                return resolveDateValue((String) val);
            case "array":
                return val;
            case "user":
                return val;
            default:
                return val;
        }
    }

    public static String resolveDateValue(String val) {
        String queryValue = "{";
//        String[] items = val.split(" ");
//        if (items.length != 2) return "";
//        switch (items[0]) {
//            case ">":
//            case ">=":
//                queryValue += "$qte:\"ISO-8601 ";
//                break;
//            case "<":
//            case "<=":
//                queryValue += "$lte:\"ISO-8601 ";
//                break;
//            default:
//                return "";
//        }
//
//        queryValue += val + "T00:00:00.000Z\"}";
        return queryValue;
    }

    protected BooleanBuilder constructPredicateTree(List<com.querydsl.core.types.Predicate> elementaryPredicates, BiFunction<BooleanBuilder, com.querydsl.core.types.Predicate, BooleanBuilder> nodeOperation) {
        if (elementaryPredicates.size() == 0)
            return new BooleanBuilder();

        ArrayDeque<BooleanBuilder> subtrees = new ArrayDeque<>(elementaryPredicates.size() / 2 + elementaryPredicates.size() % 2);

        for (Iterator<com.querydsl.core.types.Predicate> predicateIterator = elementaryPredicates.iterator(); predicateIterator.hasNext(); ) {
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
