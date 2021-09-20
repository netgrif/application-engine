package com.netgrif.workflow.workflow.domain;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.UntypedObjectDeserializer;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

@SuppressWarnings({ "deprecation", "serial" })
public class CustomFilterDeserializer extends UntypedObjectDeserializer {

    private static final CustomFilterDeserializer INSTANCE = new CustomFilterDeserializer();

    private CustomFilterDeserializer() {}

    public static CustomFilterDeserializer getInstance() {
        return INSTANCE;
    }

    public static String[] listValues = {"filter", "allowedNet", "searchCategory", "predicateMetadataItem", "predicate",
            "stringValue", "integerValue", "booleanValue", "mapValue", "longValue"
    };

    @Override
    @SuppressWarnings({ "unchecked"})
    protected Object mapObject(JsonParser parser, DeserializationContext context) throws IOException {

        @Nullable String firstKey;
        JsonToken token = parser.getCurrentToken();
        if (token == JsonToken.START_OBJECT) {
            firstKey = parser.nextFieldName();
        } else if (token == JsonToken.FIELD_NAME) {
            firstKey = parser.getCurrentName();
        } else {
            if (token != JsonToken.END_OBJECT) {
                throw context.mappingException(handledType(), parser.getCurrentToken());
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