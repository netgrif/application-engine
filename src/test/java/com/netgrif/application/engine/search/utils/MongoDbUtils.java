package com.netgrif.application.engine.search.utils;

import com.querydsl.core.types.Predicate;

import org.bson.Document;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.repository.support.SpringDataMongodbQuery;

public class MongoDbUtils<T> extends SpringDataMongodbQuery<T> {

    public MongoDbUtils(MongoOperations operations, Class<? extends T> type) {
        super(operations, type, operations.getCollectionName(type));
    }

    public Document convertPredicateToDocument(Predicate predicate) {
        return this.createQuery(predicate);
    }
}
