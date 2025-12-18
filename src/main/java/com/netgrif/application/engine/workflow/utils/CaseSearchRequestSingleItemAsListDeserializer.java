package com.netgrif.application.engine.workflow.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.netgrif.application.engine.elastic.service.ElasticsearchQuerySanitizer;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleCaseSearchRequestAsList;
import com.netgrif.application.engine.utils.SingleItemAsList;
import com.netgrif.application.engine.utils.SingleItemAsListDeserializer;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

public class CaseSearchRequestSingleItemAsListDeserializer extends SingleItemAsListDeserializer {

    protected CaseSearchRequestSingleItemAsListDeserializer() {
        super();
    }

    protected CaseSearchRequestSingleItemAsListDeserializer(Class<? extends SingleItemAsList> vc) {
        super(vc);
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
