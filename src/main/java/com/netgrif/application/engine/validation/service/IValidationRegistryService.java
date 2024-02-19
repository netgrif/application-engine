package com.netgrif.application.engine.validation.service;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.workflow.domain.DataField;
import com.netgrif.application.engine.workflow.domain.Task;

public interface IValidationRegistryService {
    void validate(Task task);

    <T extends Field<?>> void validate(T field, DataField dataField);
}
