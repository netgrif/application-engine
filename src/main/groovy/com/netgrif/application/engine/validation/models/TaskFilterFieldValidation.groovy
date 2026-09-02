package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.pfql.domain.enums.QueryType
import com.netgrif.application.engine.validation.domain.ValidationDataInput

class TaskFilterFieldValidation extends FilterFieldValidation{

    @Override
    void query(ValidationDataInput validationData) {
        doValidation(validationData, QueryType.TASK)
    }
}
