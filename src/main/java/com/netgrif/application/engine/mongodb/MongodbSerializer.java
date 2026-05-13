package com.netgrif.application.engine.mongodb;

import com.mongodb.DBRef;
import com.querydsl.core.types.Path;
import com.querydsl.mongodb.document.MongodbDocumentSerializer;

public class MongodbSerializer extends MongodbDocumentSerializer {

    @Override
    protected DBRef asReference(Object o) {
        return null;
    }

    @Override
    protected boolean isReference(Path<?> path) {
        return false;
    }
}
