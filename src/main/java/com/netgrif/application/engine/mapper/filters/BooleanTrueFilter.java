package com.netgrif.application.engine.mapper.filters;

public class BooleanTrueFilter {

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return true;
        }
        return obj.equals(true);
    }
}
