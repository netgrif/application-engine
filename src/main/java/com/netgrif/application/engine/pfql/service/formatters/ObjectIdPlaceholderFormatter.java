package com.netgrif.application.engine.pfql.service.formatters;

import org.bson.types.ObjectId;

// todo 2483 doc
public class ObjectIdPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof ObjectId;
    }

    @Override
    public String format(Object value) {
        return wrapInSingleQuotes(((ObjectId) value).toHexString());
    }
}
