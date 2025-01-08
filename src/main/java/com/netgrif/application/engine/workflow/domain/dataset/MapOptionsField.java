package com.netgrif.application.engine.workflow.domain.dataset;

import com.netgrif.application.engine.workflow.domain.dataset.logic.Expression;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public abstract class MapOptionsField<T, U> extends Field<U> {

    protected LinkedHashMap<String, T> options;
    protected Expression<Map<String, T>> optionsExpression;

    public boolean isDynamic() {
        return this.optionsExpression != null;
    }
}
