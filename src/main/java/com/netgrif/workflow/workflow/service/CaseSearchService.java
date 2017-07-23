package com.netgrif.workflow.workflow.service;

import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.repositories.UserRepository;
import com.netgrif.workflow.workflow.domain.Case;
import org.apache.log4j.Logger;
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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

@Service
public class CaseSearchService {

    private static final Logger log = Logger.getLogger(CaseSearchService.class.getName());
    private static final String ERROR_KEY = "ERROR";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public Page<Case> search(Map<String, Object> searchRequest, Pageable pageable) {
        try {
            return executeQuery(buildQuery(resolveRequest(searchRequest)), pageable);
        } catch (IllegalQueryException e) {
            e.printStackTrace();
            return new PageImpl<>(new ArrayList<>(), pageable, 0);
        }
    }

    private Map<String, Object> resolveRequest(Map<String, Object> request) {
        Map<String, Object> queryParts = new LinkedHashMap<>();
        boolean match = request.entrySet().stream().allMatch((Map.Entry<String, Object> entry) -> {
            try {
                Method method = this.getClass().getMethod(entry.getKey() + "Query", Object.class);
                log.info("RESOLVED METHOD: " + entry.getKey());
                Object part = method.invoke(this, entry.getValue());
                if (part != null) //TODO 23.7.2017 throw exception when cannot build query
                    queryParts.put(entry.getKey(), part);
                return true;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
                queryParts.put(ERROR_KEY, "Parameter " + entry.getKey() + " is not supported in Case search!");
                return false;
            }
        });

        return queryParts;
    }

    private String buildQuery(Map<String, Object> queryParts) throws IllegalQueryException {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        boolean result = queryParts.entrySet().stream().allMatch(entry -> {
            if (entry.getKey().equals(ERROR_KEY)) return false;
            if (((String) entry.getValue()).endsWith(":")) {
                queryParts.put(ERROR_KEY, "Query attribute " + entry.getKey() + " has wrong value!");
                return false;
            }

            log.info("CASE QUERY: " + entry.getValue());
            builder.append(entry.getValue());
            builder.append(",");
            return true;
        });
        if (!result)
            throw new IllegalQueryException((String) (queryParts.get(ERROR_KEY)));

        builder.deleteCharAt(builder.length() - 1);
        builder.append("}");
        return builder.toString();
    }

    private Page<Case> executeQuery(String queryString, Pageable pageable) {
        Query query = new BasicQuery(queryString).with(pageable);
        log.info("CASE SEARCH QUERY RUNNING: " + queryString);
        return new PageImpl<>(mongoTemplate.find(query, Case.class),
                pageable,
                mongoTemplate.count(new BasicQuery(queryString, "{_id:1}"), Case.class));
    }


    // **************************
    // * Query building methods *
    // **************************

    public String idQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> in((List<Object>) obj, BSONType.ObjectId, null,null));
        builder.put(String.class, o -> oid((String) o));

        return buildQueryPart("_id", obj, builder);
    }

    public String authorQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(Long.class, o -> (String) o);
        builder.put(Integer.class, o -> (String) o);
        builder.put(ArrayList.class, o -> in((List<Object>) obj, BSONType.Number, ob -> ob instanceof Long || ob instanceof Integer, null));
        builder.put(String.class, o -> {
            Long id = resolveAuthorByEmail((String) obj);
            return id != null ? id.toString() : "";
        });

        return buildQueryPart("author", obj, builder);
    }

    public String titleQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(ArrayList.class, o -> in(((List<Object>) obj), BSONType.String, null,null));
        builder.put(String.class, o -> (String) o);

        return buildQueryPart("title", obj, builder);
    }

    public String petriNetQuery(Object obj) {
        Map<Class, Function<Object, String>> builder = new HashMap<>();

        builder.put(String.class, o ->ref("petriNet",obj));
        builder.put(ArrayList.class, o->in(((List<Object>)obj),BSONType.Ref,null,new String[]{"petriNet"}));

        return buildQueryPart("petriNet",obj,builder);
    }


    // ***********************************************
    // * Helper methods for building mongodb queries *
    // ***********************************************

    private String buildQueryPart(String attribute, Object obj, Map<Class, Function<Object, String>> builder) {
        return "\"" +
                attribute +
                "\":" +
                builder.get(obj.getClass()).apply(obj);
    }

    private Long resolveAuthorByEmail(String email) {
        User user = userRepository.findByEmail(email);
        return user != null ? user.getId() : null;
    }

    public static String oid(String id) {
        return "{$oid:\"" + id + "\"}";
    }

    public static String in(List<Object> objects, BSONType type, Predicate<Object> typeTest, Object[] typeArgs) {
        return in(objects.stream().collect(Collectors.toMap(Function.identity(), o -> type,
                (v1, v2) -> type, LinkedHashMap::new)), typeTest, typeArgs);
    }

    public static String in(Map<Object, BSONType> values, Predicate<Object> typeTest, Object[] typeArgs) {
        StringBuilder builder = new StringBuilder();
        builder.append("{$in:[");
        values.forEach((o, type) -> {
            if (typeTest != null && !typeTest.test(o)) return;

            if (type == BSONType.ObjectId) builder.append(oid((String) o));
            if(type == BSONType.Ref)builder.append(ref((String)typeArgs[0],o));
            else builder.append(o);

            builder.append(",");
        });
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]}");
        return builder.toString();
    }

    public static String ref(String attr, Object id) {
        return "{$ref:\"" + attr + "\",$id:" + oid((String) id) + "}";
    }


    public enum BSONType {
        ObjectId,
        Number,
        String,
        Ref,
        Undefined
    }


}
