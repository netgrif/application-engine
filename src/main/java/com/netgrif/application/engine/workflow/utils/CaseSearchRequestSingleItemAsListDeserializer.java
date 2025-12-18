package com.netgrif.application.engine.workflow.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.netgrif.application.engine.elastic.service.ElasticsearchQuerySanitizer;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleCaseSearchRequestAsList;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.utils.SingleItemAsListDeserializer;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

@Slf4j
public class CaseSearchRequestSingleItemAsListDeserializer extends SingleItemAsListDeserializer {

    protected CaseSearchRequestSingleItemAsListDeserializer() {
        this(null);
    }

    protected CaseSearchRequestSingleItemAsListDeserializer(Class<? extends SingleItemAsList> vc) {
        super(vc);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        final JavaType type;
        if (beanProperty != null)
            type = beanProperty.getType();
        else
            type = deserializationContext.getContextualType();

        return new CaseSearchRequestSingleItemAsListDeserializer((Class<? extends SingleItemAsList>) type.getRawClass());
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, IllegalArgumentException {
        Object result = super.deserialize(jsonParser, deserializationContext);
        if (isCaseSearchRequestWrapper(result)) {
            List<CaseSearchRequest> list = ((SingleCaseSearchRequestAsList) result).getList();
            list.forEach(request ->
                    request.fullText = ElasticsearchQuerySanitizer.sanitize(request.fullText));
        }
        return result;
    }

    protected boolean isCaseSearchRequestWrapper(Object object) {
        try {
            Type superClass = object.getClass().getGenericSuperclass();
            return object instanceof SingleCaseSearchRequestAsList ||
                    (superClass != null &&
                            ((ParameterizedType) superClass).getActualTypeArguments()[0] == CaseSearchRequest.class);
        } catch (Exception e) {
            return false;
        }
    }
}
