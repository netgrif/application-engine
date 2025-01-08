package com.netgrif.application.engine.utils;

import com.netgrif.application.engine.workflow.domain.dataset.Field;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.PropertyUtilsBean;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;

public class FieldUtils extends BeanUtilsBean {

    final PropertyUtilsBean propertyUtils;

    public FieldUtils() {
        super();
        this.propertyUtils = getPropertyUtils();
    }

    @Override
    public void copyProperties(Object dest, Object orig) throws IllegalAccessException, InvocationTargetException {
        if (notFields(dest, orig)) {
            throw new IllegalArgumentException();
        }
        Field<?> destination = (Field<?>) dest;
        Field<?> original = (Field<?>) orig;
        final PropertyDescriptor[] origDescriptors = propertyUtils.getPropertyDescriptors(original);

        for (PropertyDescriptor origDescriptor : origDescriptors) {
            final String name = origDescriptor.getName();
            if ("class".equals(name)) {
                continue; // No point in trying to set an object's class
            }
            if (canNotCopyProperty(destination, original, name)) {
                continue;
            }
            try {
                final Object value = propertyUtils.getSimpleProperty(original, name);
                copyProperty(destination, name, value);
            } catch (final NoSuchMethodException e) {
                // Should not happen
            }
        }
    }

    @Override
    public void copyProperty(Object bean, String name, Object value) throws IllegalAccessException, InvocationTargetException {
        if (value == null) {
            return;
        }
        super.copyProperty(bean, name, value);
    }

    private boolean notFields(Object dest, Object orig) {
        return !(dest instanceof Field<?>) || !(orig instanceof Field<?>);
    }

    private boolean canNotCopyProperty(Field<?> destination, Field<?> original, String propertyName) {
        if ("value".equals(propertyName)) {
            return false;
        }
        return !(propertyUtils.isReadable(original, propertyName) && propertyUtils.isWriteable(destination, propertyName));
    }
}
