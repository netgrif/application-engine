package com.netgrif.application.engine.utils;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import org.apache.commons.beanutils.BeanUtilsBean;

import java.lang.reflect.InvocationTargetException;

public class FieldUtils extends BeanUtilsBean {

    @Override
    public void copyProperties(Object dest, Object orig) throws IllegalAccessException, InvocationTargetException {
        if (!(dest instanceof Field<?>) || !(orig instanceof Field<?>)) {
            throw new IllegalArgumentException();
        }
        super.copyProperties(dest, orig);
    }

    @Override
    public void copyProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        if (value == null) {
            return;
        }
        super.copyProperty(bean, name, value);
    }
}
