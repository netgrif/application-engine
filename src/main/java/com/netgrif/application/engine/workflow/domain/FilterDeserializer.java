package com.netgrif.application.engine.workflow.domain;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;
import com.fasterxml.jackson.databind.util.ClassUtil;
import org.springframework.lang.Nullable;

import java.io.IOException;
import java.util.*;

/**
 * Class that helps with deserialization of exported filter xml file in process of importing filters.
 */

public class FilterDeserializer extends UntypedObjectDeserializer {

    private static final FilterDeserializer INSTANCE = new FilterDeserializer(null, null);

    private FilterDeserializer(JavaType listType, JavaType mapType) {
        super(listType, mapType);
    }

    public static FilterDeserializer getInstance() {
        return INSTANCE;
    }

    public static String[] listValues = {"filter", "allowedNet", "searchCategory", "predicateMetadataItem", "predicate",
            "stringValue", "doubleValue", "booleanValue", "mapValue", "longValue"
    };

    @Override
    protected Object mapObject(JsonParser parser, DeserializationContext context) throws IOException {

        String firstKey;
        JsonToken token = parser.getCurrentToken();
        if (token == JsonToken.START_OBJECT) {
            firstKey = parser.nextFieldName();
        } else if (token == JsonToken.FIELD_NAME) {
            firstKey = parser.getCurrentName();
        } else {
            if (token != JsonToken.END_OBJECT) {
                throw JsonMappingException.from(parser,
                        String.format("Cannot deserialize instance of %s out of %s token",
                                ClassUtil.nameOf(handledType()), token));
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

        } while ((nextKey = parser.nextFieldName()) != null);


        return objectList.size() == 0 ? valueByKey : objectList;
    }

}