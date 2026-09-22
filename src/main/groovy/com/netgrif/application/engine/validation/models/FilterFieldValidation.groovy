package com.netgrif.application.engine.validation.models

import com.netgrif.application.engine.pfql.domain.enums.QueryType
import com.netgrif.application.engine.pfql.service.QueryLangEvaluator
import com.netgrif.application.engine.pfql.service.utils.SearchUtils
import com.netgrif.application.engine.validation.domain.ValidationDataInput

/**
 * Abstract base class for validating filter field values against PFQL queries.
 * <p>
 * This class provides common validation logic for filter fields that need to verify
 * whether their values represent valid PFQL queries of a specific type.
 * </p>
 */
abstract class FilterFieldValidation extends AbstractFieldValidation {

    /**
     * Validates the filter field value as a PFQL query.
     * <p>
     * Implementations must specify the expected query type for validation.
     * </p>
     *
     * @param validationData the validation data input containing the field value and validation context
     */
    abstract void query(ValidationDataInput validationData)

    /**
     * Performs validation of the field value against a specific PFQL query type.
     * <p>
     * This method checks if the field value is a valid PFQL query and verifies that
     * it matches the expected query type. If the value is null or empty, no validation
     * is performed. If the query is invalid or does not match the expected type,
     * an IllegalArgumentException is thrown with the localized validation message.
     * </p>
     *
     * @param validationData the validation data input containing the field value, locale, and validation message
     * @param queryType the expected PFQL query type that the field value should match
     * @throws IllegalArgumentException if the query is invalid or does not match the expected type
     */
    protected static void doValidation(ValidationDataInput validationData, QueryType queryType) {
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