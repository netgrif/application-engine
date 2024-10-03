package com.netgrif.application.engine.petrinet.domain.dataset;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.Expression;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public abstract class MapOptionsField<T, U> extends Field<U> {

    protected Map<String, T> options;
    protected Expression<Map<String, T>> optionsExpression;

    public boolean isDynamic() {
        return this.optionsExpression != null;
    }
}
