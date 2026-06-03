package com.netgrif.application.engine.utils;

import com.querydsl.core.types.Predicate;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.repository.support.SpringDataMongodbQuery;

public final class MongodbUtils {

    private MongodbUtils() {
    }

    public static <T> Query toQuery(MongoTemplate mongoTemplate, Class<T> type, Predicate... predicate) {
        SpringDataMongodbQuery<T> springDataMongodbQuery = new SpringDataMongodbQuery<>(mongoTemplate, type).where(predicate);
        return new BasicQuery(springDataMongodbQuery.asDocument());
    }
}
