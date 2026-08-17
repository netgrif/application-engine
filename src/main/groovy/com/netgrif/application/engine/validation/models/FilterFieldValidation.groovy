package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.pfql.domain.enums.QueryType
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator
import com.netgrif.application.engine.pfql.service.utils.SearchUtils
import com.netgrif.application.engine.validation.domain.ValidationDataInput

abstract class FilterFieldValidation extends AbstractFieldValidation {
    // todo 2466 generate doc
    abstract void query(ValidationDataInput validationData)

    // todo 2466 generate doc
    protected void doValidation(ValidationDataInput validationData, QueryType queryType) {
        if (validationData.getData().getValue() != null && validationData.getData().getValue() != "") {
            try {
                QueryLangEvaluator evaluator = SearchUtils.evaluateQuery(validationData.getData().getValue() as String)
                if (evaluator.resourceType !== queryType) {
                    throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
                }
            } catch (Exception ignore) {
                throw new IllegalArgumentException(validationData.getValidationMessage().getTranslation(validationData.getLocale()))
            }
        }
    }
}