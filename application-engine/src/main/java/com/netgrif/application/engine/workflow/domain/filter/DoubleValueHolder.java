package com.netgrif.application.engine.workflow.domain.filter;

public abstract class DoubleValueHolder {
    protected Double convertObjectToDouble(Object val) {
        if (val instanceof Double)
            return (Double) val;
        else if (val instanceof Integer)
            return new Double((Integer) val);
        else if (val instanceof Float)
            return new Double((Float) val);
        else if (val instanceof String)
            return Double.parseDouble((String) val);
        throw new IllegalArgumentException("The provided Object (" + val.toString() + ") cannot be converted to Double");
    }
}
