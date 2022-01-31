package com.netgrif.application.engine.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.List;

public class SingleItemAsListDeserializer extends StdDeserializer<Object> implements ContextualDeserializer {

    private Class<?> itemClass;

    protected SingleItemAsListDeserializer() {
        this(null);
    }

    protected SingleItemAsListDeserializer(Class<? extends SingleItemAsList> vc) {
        super(vc);
        if (vc != null)
            this.itemClass = vc.getAnnotation(JsonDeserialize.class).contentAs();
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        final JavaType type;
        if (beanProperty != null)
            type = beanProperty.getType();
        else
            type = deserializationContext.getContextualType();

        return new SingleItemAsListDeserializer((Class<? extends SingleItemAsList>) type.getRawClass());
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, IllegalArgumentException {
        SingleItemAsList<Object> wrapper;
        try {
            wrapper = (SingleItemAsList<Object>) this._valueClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("SingleItemAsList object could not be instantiated", e);
        }

        ObjectMapper innerDeserializer = new ObjectMapper();
        JsonNode node = innerDeserializer.readTree(jsonParser);
        try {
            Object request = innerDeserializer.convertValue(node, this.itemClass);
            wrapper.getList().add(request);
        } catch (IllegalArgumentException singleItemException) {
            try {
                List<?> requests = innerDeserializer.convertValue(node, innerDeserializer.getTypeFactory().constructCollectionType(List.class, this.itemClass));
                wrapper.getList().addAll(requests);
            } catch (IllegalArgumentException arrayException) {
                if (node.isArray())
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Single item as list deserialization failed. List deserialization exception: " + arrayException.getMessage(), arrayException);
                else
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Single item as list deserialization failed. Single item deserialization exception: " + singleItemException.getMessage(), singleItemException);
            }
        }

        return wrapper;
    }
}
