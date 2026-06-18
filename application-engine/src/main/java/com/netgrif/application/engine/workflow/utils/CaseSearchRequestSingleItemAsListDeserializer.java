package com.netgrif.application.engine.workflow.utils;

import com.netgrif.application.engine.elastic.service.ElasticsearchQuerySanitizer;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleCaseSearchRequestAsList;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.utils.SingleItemAsListDeserializer;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.io.IOException;
import java.util.List;

/**
 * Custom deserializer for handling JSON deserialization of objects that extend
 * the {@link SingleItemAsList} class, specifically designed for handling
 * {@link CaseSearchRequest} and ensuring its fields are properly sanitized.
 * <p>
 * This deserializer extends the functionality of {@link SingleItemAsListDeserializer}
 * to additionally process the deserialized objects that represent case search requests.
 * It ensures that the `fullText` field in each case search request is sanitized
 * using {@link ElasticsearchQuerySanitizer}.
 * <p>
 * It also provides a mechanism to dynamically determine the appropriate type
 * using the contextual information during deserialization.
 */
@Slf4j
public class CaseSearchRequestSingleItemAsListDeserializer extends SingleItemAsListDeserializer {

    public CaseSearchRequestSingleItemAsListDeserializer() {
        this(null);
        log.info("Creating CaseSearchRequestSingleItemAsListDeserializer");
    }

    protected CaseSearchRequestSingleItemAsListDeserializer(Class<? extends SingleItemAsList> vc) {
        super(vc);
        log.info("Creating CaseSearchRequestSingleItemAsListDeserializer with vc: {}", vc);
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        log.info("Creating contextual CaseSearchRequestSingleItemAsListDeserializer");
        return new CaseSearchRequestSingleItemAsListDeserializer(
                (Class<? extends SingleItemAsList>) getItemClass(deserializationContext, beanProperty)
        );
    }

    /**
     * Deserializes a JSON structure into an object, specifically handling instances that
     * may extend the {@code SingleCaseSearchRequestAsList}. During deserialization, it
     * sanitizes the `fullText` field in each {@code CaseSearchRequest} object for security
     * purposes using {@code ElasticsearchQuerySanitizer}.
     *
     * @param jsonParser             the {@code JsonParser} used for reading the JSON input
     * @param deserializationContext the {@code DeserializationContext} providing access
     *                               to contextual information during deserialization
     * @return the deserialized object, with sanitization applied if it is an instance of
     * {@code SingleCaseSearchRequestAsList}
     * @throws IllegalArgumentException if the object could not be properly instantiated or deserialized
     */
    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IllegalArgumentException {
        log.info("Deserializing CaseSearchRequestSingleItemAsList");
        log.info("Deserializing CaseSearchRequestSingleItemAsList with jsonParser: {}", jsonParser);

        Object result = null;
        result = super.deserialize(jsonParser, deserializationContext);

        log.info("Deserializing CaseSearchRequestSingleItemAsList with result: {}", result);

        if (isWrapperClass(result, SingleCaseSearchRequestAsList.class, CaseSearchRequest.class)) {
            List<CaseSearchRequest> list = ((SingleCaseSearchRequestAsList) result).getList();
            list.forEach(request -> {
                if (request != null) {
                    request.fullText = ElasticsearchQuerySanitizer.sanitize(request.fullText);
                }
            });
        }

        log.info("Deserializing CaseSearchRequestSingleItemAsList with result: {}", result);
        return result;
    }

}