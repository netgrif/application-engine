package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.DataType;
import com.netgrif.application.engine.importer.service.Importer;
import com.netgrif.application.engine.workflow.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.dataset.logic.Expression;
import com.netgrif.application.engine.utils.UniqueKeyMap;
import com.netgrif.application.engine.workflow.domain.DataFieldBehaviors;
import org.bson.types.ObjectId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.function.Function;

public abstract class FieldBuilder<T extends Field<?>> {

    public abstract T build(Data data, Importer importer);

    public abstract DataType getType();

    public void initialize(Field<?> field) {
        field.setId(new ObjectId());
        field.setEvents(new HashMap<>());
        field.setBehaviors(new DataFieldBehaviors());
        field.setValidations(new ArrayList<>());
        field.setProperties(new UniqueKeyMap<>());
    }

    public <U> void setDefaultValue(Field<U> field, Data data) {
        setDefaultValue(field, data, null);
    }

    public <U> void setDefaultValue(Field<U> field, Data data, Function<String, U> staticValue) {
        String definition = getInitExpression(data);
        Expression<U> initExpression = null;
        if (definition != null) {
            initExpression = Expression.ofDynamic(definition);
        } else {
            String init = resolveInit(data);
            if (init != null && staticValue != null) {
                initExpression = Expression.ofStatic(staticValue.apply(init));
            }
        }
        field.setDefaultValue(initExpression);
    }

    public String getInitExpression(Data data) {
        if (data.getInit() != null) {
            if (data.getInit().isDynamic()) {
                return data.getInit().getValue();
            }
        }
        return null;
    }

    public String resolveInit(Data data) {
        if (data.getInit() == null) {
            return null;
        }
        return data.getInit().getValue();
    }
}
