package com.netgrif.application.engine.importer.service.validation;


import lombok.extern.slf4j.Slf4j;

import java.util.Collection;

@Slf4j
public abstract class ModelValidator implements IModelValidator {

//    @Override
//    public void validateAttribute(Object attr, String attrName) {
//        if (attr instanceof Collection && !((Collection<?>) attr).isEmpty()) {
//            log.warn("Data attribute [" + attrName + "] is deprecated.");
//        }
//
//        if (!(attr instanceof Collection) && attr != null) {
//            log.warn("Data attribute [" + attrName + "] is deprecated.");
//        }
//    }
}
