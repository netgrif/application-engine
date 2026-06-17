package com.netgrif.application.engine.validation.service.interfaces

import com.netgrif.application.engine.objects.petrinet.domain.dataset.Field
import com.netgrif.application.engine.objects.workflow.domain.DataField


interface IValidationService {

    public void valid(Field field, DataField dataField);

}
