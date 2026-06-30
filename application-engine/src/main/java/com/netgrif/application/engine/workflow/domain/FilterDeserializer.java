package com.netgrif.application.engine.workflow.domain;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.deser.jdk.UntypedObjectDeserializer;
import tools.jackson.databind.util.ClassUtil;

import java.util.*;

/**
 * Class that helps with deserialization of exported filter xml file in process of importing filters.
 */

public class FilterDeserializer extends UntypedObjectDeserializer {

    private static final FilterDeserializer INSTANCE = new FilterDeserializer(null, null);

    public static String[] listValues = {"filter", "allowedNet", "searchCategory", "predicateMetadataItem", "predicate",
            "stringValue", "doubleValue", "booleanValue", "mapValue", "longValue"
    };

    private FilterDeserializer(JavaType listType, JavaType mapType) {
        super(listType, mapType);
    }

    public static FilterDeserializer getInstance() {
        return INSTANCE;
    }

    @Override
    protected Object mapObject(JsonParser parser, DeserializationContext context) throws JacksonException {

        String firstKey;
        JsonToken token = parser.currentToken();
        if (token == JsonToken.START_OBJECT) {
            firstKey = parser.nextName();
        } else if (token == JsonToken.PROPERTY_NAME) {
            firstKey = parser.currentName();
        } else {
            if (token != JsonToken.END_OBJECT) {
                return context.reportInputMismatch(handledType(),
                        "Cannot deserialize instance of %s out of %s token",
                        ClassUtil.nameOf(handledType()), token);
            }
            return Collections.emptyMap();
        }

        Map<String, Object> valueByKey = new LinkedHashMap<>();
        List<Object> objectList = new ArrayList<>();
        String nextKey = firstKey;
        do {
            parser.nextToken();
            Object nextValue = deserialize(parser, context);

            if (Arrays.asList(listValues).contains(nextKey)) {
                objectList.add(nextValue);
            } else {
                valueByKey.put(nextKey, nextValue);
            }

        } while ((nextKey = parser.nextName()) != null);


        return objectList.size() == 0 ? valueByKey : objectList;
    }

}
