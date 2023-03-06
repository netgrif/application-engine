package com.netgrif.application.engine.validation.service.interfaces

import com.netgrif.application.engine.petrinet.domain.dataset.Field

interface IValidationService {

    void valid(Field<?> dataField);
}