package com.netgrif.application.engine.validation.validator;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.validation.exception.ValidationException;
import com.netgrif.application.engine.workflow.domain.DataField;

import java.text.ParseException;

public interface IValidator<T extends Field<?>> {
    void validate(T field, DataField dataField) throws ValidationException, ParseException;
    String getName();
}
