package com.netgrif.application.engine.elastic.service.query;

import com.netgrif.application.engine.authentication.domain.LoggedIdentity;
import org.elasticsearch.index.query.BoolQueryBuilder;

import javax.annotation.Nullable;
import java.util.Locale;

public interface ElasticQueryBuilder {
    <T> BoolQueryBuilder buildSingleQuery(T request, Locale locale, @Nullable LoggedIdentity identity);
}
