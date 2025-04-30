package com.netgrif.application.engine.objects.workflow.domain.filter;

public abstract class DoubleValueHolder {
    protected Double convertObjectToDouble(Object val) {
        if (val instanceof Double)
            return (Double) val;
        else if (val instanceof Integer)
            return Double.valueOf((Integer) val);
        else if (val instanceof Float)
            return Double.valueOf((Float) val);
        else if (val instanceof String)
            return Double.parseDouble((String) val);
        throw new IllegalArgumentException("The provided Object (" + val.toString() + ") cannot be converted to Double");
    }
}
