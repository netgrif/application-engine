package com.netgrif.application.engine.importer.service;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public interface IModelValidator {
    default void validateAttribute(Object attr, String attrName) {
        Logger log = LoggerFactory.getLogger(IModelValidator.class);

        if (attr instanceof Collection && !((Collection<?>) attr).isEmpty()) {
            log.warn("Data attribute [" + attrName + "] is deprecated.");
        }

        if (!(attr instanceof Collection) && attr != null) {
            log.warn("Data attribute [" + attrName + "] is deprecated.");
        }
    }
}
