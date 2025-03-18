package com.netgrif.application.engine.validation.service.interfaces

import com.netgrif.core.petrinet.domain.dataset.Field
import com.netgrif.core.workflow.domain.DataField


interface IValidationService {

    public void valid(Field field, DataField dataField);

}