package com.netgrif.application.engine.elastic.service.query;

import org.elasticsearch.index.query.BoolQueryBuilder;

import javax.annotation.Nullable;
import java.util.Locale;

public interface ElasticQueryBuilder {
    <T> BoolQueryBuilder buildSingleQuery(T request, Locale locale, @Nullable String actorId,
                                          @Nullable BoolQueryBuilder permissionQuery);
}
