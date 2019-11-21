package com.netgrif.workflow.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Getter
@JsonDeserialize(using = SingleItemAsList.SingleItemAsListDeserializer.class)
public class SingleItemAsList<T> {

    private List<T> list;

    private SingleItemAsList() {
        this.list = new ArrayList<>();
    }

    private SingleItemAsList(T item) {
        this();
        this.list.add(item);
    }

    private SingleItemAsList(List<T> list) {
        this.list = list;
    }


    static class SingleItemAsListDeserializer<T> extends StdDeserializer<SingleItemAsList<T>> {

        protected SingleItemAsListDeserializer() {
            this(null);
        }

        SingleItemAsListDeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public SingleItemAsList<T> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
            SingleItemAsList<T> wrapper;

            ObjectMapper deserializer = new ObjectMapper();
            JsonNode node = deserializer.readTree(jsonParser);
            try {
                T singleItem = deserializer.convertValue(node, new TypeReference<T>() {
                });
                wrapper = new SingleItemAsList<>(singleItem);
            } catch (IllegalArgumentException singleItemParsingException) {
                List<T> list = deserializer.convertValue(node, new TypeReference<List<T>>() {
                });
                wrapper = new SingleItemAsList<>(list);
            }

            return wrapper;
        }
    }

//    static class SingleItemAsListDeserializer extends JsonDeserializer<SingleItemAsList<?>> implements ContextualDeserializer {
//        private JavaType wrappedType;
//
//        @Override
//        public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) throws JsonMappingException {
//            SingleItemAsListDeserializer deserializer = new SingleItemAsListDeserializer();
//            deserializer.wrappedType = beanProperty.getType().containedType(0);
//            return deserializer;
//        }
//
//        @Override
//        public SingleItemAsList<?> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
//            SingleItemAsList<?> wrapper;
//            try {
//                wrapper = new SingleItemAsList<>(deserializationContext.readValue(jsonParser, wrappedType));
//            } catch (IOException e) {
//                JavaType listType = deserializationContext.getTypeFactory().constructCollectionType(List.class, wrappedType);
//                wrapper = new SingleItemAsList<>(deserializationContext.readValue(jsonParser, listType));
//            }
//            return wrapper;
//        }
//    }
}
