package com.netgrif.application.engine.workflow.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.netgrif.application.engine.elastic.service.ElasticsearchQuerySanitizer;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.utils.SingleItemAsListDeserializer;
import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class TaskSearchRequestSingleItemAsListDeserializer extends SingleItemAsListDeserializer {

    protected TaskSearchRequestSingleItemAsListDeserializer() {
        this(null);
    }

    protected TaskSearchRequestSingleItemAsListDeserializer(Class<? extends SingleItemAsList> vc) {
        super(vc);
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        final JavaType type;
        if (beanProperty != null)
            type = beanProperty.getType();
        else
            type = deserializationContext.getContextualType();

        return new TaskSearchRequestSingleItemAsListDeserializer((Class<? extends SingleItemAsList>) type.getRawClass());
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, IllegalArgumentException {
        Object result = super.deserialize(jsonParser, deserializationContext);
        if (isTaskSearchRequestWrapper(result)) {
            List<TaskSearchRequest> list = ((SingleTaskSearchRequestAsList) result).getList();
            list.forEach(request ->
                    request.fullText = ElasticsearchQuerySanitizer.sanitize(request.fullText));
        }
        return result;
    }

    protected boolean isTaskSearchRequestWrapper(Object object) {
        try {
            Type superClass = object.getClass().getGenericSuperclass();
            return object instanceof SingleTaskSearchRequestAsList ||
                    (superClass != null &&
                            ((ParameterizedType) superClass).getActualTypeArguments()[0] == TaskSearchRequest.class);
        } catch (Exception e) {
            return false;
        }
    }
}
