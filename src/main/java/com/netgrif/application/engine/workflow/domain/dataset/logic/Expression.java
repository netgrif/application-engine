package com.netgrif.application.engine.workflow.domain.dataset.logic;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class Expression<T> implements Serializable {

    private static final long serialVersionUID = 3687481111847498422L;

    private String id;
    private T defaultValue;
    private String definition;

    protected Expression(T defaultValue, String definition) {
        this.id = new ObjectId().toString();
        this.defaultValue = defaultValue;
        this.definition = definition;
    }

    public static <T> Expression<T> ofStatic(T defaultValue) {
        return new Expression<>(defaultValue, null);
    }

    public static <T> Expression<T> ofDynamic(String definition) {
        return new Expression<>(null, definition);
    }

    public boolean isDynamic() {
        return defaultValue == null && definition != null;
    }

    @Override
    public String toString() {
        return definition;
    }

    @Override
    public Expression<T> clone() {
        if (defaultValue != null) {
            return Expression.ofStatic(defaultValue);
        }
        return Expression.ofDynamic(definition);
    }
}
