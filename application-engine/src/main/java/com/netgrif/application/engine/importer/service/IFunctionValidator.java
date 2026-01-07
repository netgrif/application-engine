package com.netgrif.application.engine.importer.service;

import com.netgrif.application.engine.objects.importer.model.Function;

public interface IFunctionValidator extends IModelValidator {
    void checkDeprecatedAttributes(Function function);
}
