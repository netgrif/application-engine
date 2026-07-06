package com.netgrif.application.engine.utils;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.BeanProperty;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.deser.std.StdDeserializer;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;

public class SingleItemAsListDeserializer extends StdDeserializer<Object> {

    private Class<?> itemClass;

    protected SingleItemAsListDeserializer() {
        this(null);
    }

    protected SingleItemAsListDeserializer(Class<? extends SingleItemAsList> vc) {
        super(vc == null ? SingleItemAsList.class : vc);

        if (vc != null) {
            JsonDeserialize jsonDeserialize = vc.getAnnotation(JsonDeserialize.class);
            if (jsonDeserialize == null) {
                throw new IllegalArgumentException(
                        "Class [" + vc.getName() + "] must be annotated with @JsonDeserialize(contentAs = ...)"
                );
            }
            this.itemClass = jsonDeserialize.contentAs();
        }
    }

    @Override
    public ValueDeserializer<?> createContextual(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        return new SingleItemAsListDeserializer(
                (Class<? extends SingleItemAsList>) getItemClass(deserializationContext, beanProperty)
        );
    }

    protected Class<?> getItemClass(DeserializationContext deserializationContext, BeanProperty beanProperty) {
        final JavaType type;
        if (beanProperty != null)
            type = beanProperty.getType();
        else
            type = deserializationContext.getContextualType();

        return type.getRawClass();
    }

    @Override
    public Object deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IllegalArgumentException {
        if (!SingleItemAsList.class.isAssignableFrom(this._valueClass) || Objects.equals(this._valueClass, SingleItemAsList.class)) {
            throw new IllegalArgumentException("SingleItemAsList deserializer was not contextualized with concrete wrapper class");
        }

        if (this.itemClass == null) {
            throw new IllegalArgumentException("SingleItemAsList item class was not resolved from @JsonDeserialize(contentAs = ...)");
        }

        SingleItemAsList<Object> wrapper;
        try {
            wrapper = (SingleItemAsList<Object>) this._valueClass.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("SingleItemAsList object could not be instantiated", e);
        }

        ObjectMapper innerDeserializer = new ObjectMapper();
        JsonNode node;
        try {
            node = deserializationContext.readTree(jsonParser);
        } catch (Exception e) {
            throw new IllegalArgumentException("Single item as list JSON tree deserialization failed", e);
        }

        try {
            if (node.isArray()) {
                List<?> requests = innerDeserializer.convertValue(
                        node,
                        innerDeserializer.getTypeFactory().constructCollectionType(List.class, this.itemClass)
                );
                wrapper.getList().addAll(requests);
            } else {
                Object request = innerDeserializer.convertValue(node, this.itemClass);
                wrapper.getList().add(request);
            }
        } catch (IllegalArgumentException e) {
            if (node.isArray()) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Single item as list deserialization failed. List deserialization exception: " + e.getMessage(),
                        e
                );
            } else {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Single item as list deserialization failed. Single item deserialization exception: " + e.getMessage(),
                        e
                );
            }
        }

        return wrapper;
    }

    protected boolean isWrapperClass(Object object, Class<?> wrapperClass, Class<?> wrappedClass) {
        try {
            Type superClass = object.getClass().getGenericSuperclass();
            return Objects.equals(object.getClass(), wrapperClass) ||
                    (superClass != null &&
                            Objects.equals(((ParameterizedType) superClass).getActualTypeArguments()[0], wrappedClass));
        } catch (Exception e) {
            return false;
        }
    }

}