package com.netgrif.application.engine.validation.service.interfaces;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.validation.domain.ValidationException;

public interface Validation<T extends Field<?>> {

    void validate(T field) throws ValidationException, Exception;

    String getName();

}