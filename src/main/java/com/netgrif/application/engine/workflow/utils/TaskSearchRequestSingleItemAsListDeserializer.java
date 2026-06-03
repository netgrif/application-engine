package com.netgrif.application.engine.workflow.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.netgrif.application.engine.elastic.service.ElasticsearchQuerySanitizer;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleElasticTaskSearchRequestAsList;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.utils.SingleItemAsListDeserializer;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Custom deserializer for handling cases where single `TaskSearchRequest` items
 * are sent as lists or standalone entities during JSON deserialization.
 * <p>
 * This class extends the `SingleItemAsListDeserializer`, enabling support for
 * deserialization scenarios where JSON may represent either a single item or a list of items.
 * It ensures compatibility with `SingleTaskSearchRequestAsList` by sanitizing the `fullText` field
 * in each `TaskSearchRequest` instance using the `ElasticsearchQuerySanitizer`.
 */
public class TaskSearchRequestSingleItemAsListDeserializer extends SingleItemAsListDeserializer {

    protected TaskSearchRequestSingleItemAsListDeserializer() {
        this(null);
    }

    protected TaskSearchRequestSingleItemAsListDeserializer(Class<? extends SingleItemAsList> vc) {
        super(vc);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        return new TaskSearchRequestSingleItemAsListDeserializer((Class<? extends SingleItemAsList>) getItemClass(deserializationContext, beanProperty));
    }

    /**
     * Deserializes a JSON input into an object while handling cases where a single
     * `TaskSearchRequest` or a list of `TaskSearchRequest` objects is included. If
     * the object is a `SingleTaskSearchRequestAsList`, it processes each `TaskSearchRequest`
     * in the list by sanitizing the `fullText` field using `ElasticsearchQuerySanitizer`.
     *
     * @param jsonParser             the JSON parser used to parse the incoming JSON content
     * @param deserializationContext the context for deserialization, providing shared
     *                               state and configuration
     * @return the deserialized object, with sanitization applied to `TaskSearchRequest.fullText`
     * if applicable
     * @throws IOException              if an I/O error occurs during parsing
     * @throws IllegalArgumentException if the deserialization process encounters an error
     */
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, IllegalArgumentException {
        Object result = super.deserialize(jsonParser, deserializationContext);
        if (isWrapperClass(result, SingleTaskSearchRequestAsList.class, TaskSearchRequest.class) ||
                isWrapperClass(result, SingleElasticTaskSearchRequestAsList.class, ElasticTaskSearchRequest.class)) {
            List<? extends TaskSearchRequest> list = Collections.emptyList();
            if (result instanceof SingleTaskSearchRequestAsList) {
                list = ((SingleTaskSearchRequestAsList) result).getList();
            } else if (result instanceof SingleElasticTaskSearchRequestAsList) {
                list = ((SingleElasticTaskSearchRequestAsList) result).getList();
            }
            list.forEach(request ->
                    request.fullText = ElasticsearchQuerySanitizer.sanitize(request.fullText));
        }
        return result;
    }

}
