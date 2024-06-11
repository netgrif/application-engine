package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.validations.interfaces.IValidationService
import com.netgrif.application.engine.workflow.domain.Case
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Slf4j
@Component
class ValidationRunner extends AbstractOrderedCommandLineRunner {

    private static final int PAGE_SIZE = 100
    public static final String VALIDATION_FILE_NAME = "engine-processes/validations/validation.xml"
    public static final String VALIDATION_PETRI_NET_IDENTIFIER = "validation"
    public static final String VALIDATION_ACTIVE_FIELD_ID = "active"
    public static final String VALIDATION_NAME_FIELD_ID = "name"
    public static final String VALIDATION_GROOVY_DEFINITION_FIELD_ID = "validation_definition_groovy"

    @Autowired
    private ImportHelper helper

    @Autowired
    private IUserService userService

    @Autowired
    private IValidationService validationService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Override
    void run(String... strings) throws Exception {
        log.info("Starting validation runner")

        helper.upsertNet(VALIDATION_FILE_NAME, VALIDATION_PETRI_NET_IDENTIFIER)

        CaseSearchRequest request = new CaseSearchRequest()
        request.query = String.format("processIdentifier:%s AND dataSet.%s.value:true", VALIDATION_PETRI_NET_IDENTIFIER, VALIDATION_ACTIVE_FIELD_ID)
        long numberActiveValidations = elasticCaseService.count([request], userService.loggedOrSystem.transformToLoggedUser(), LocaleContextHolder.locale, false)
        int pageCount = (int) (numberActiveValidations / PAGE_SIZE) + 1
        pageCount.times {
            elasticCaseService.search([request], userService.loggedOrSystem.transformToLoggedUser(), PageRequest.of(it, PAGE_SIZE), LocaleContextHolder.locale, false)
                    .getContent()
                    .each { Case validationCase ->
                        validationService.registerValidation(
                                validationCase.getDataSet().get(VALIDATION_NAME_FIELD_ID).rawValue as String,
                                validationCase.getDataSet().get(VALIDATION_GROOVY_DEFINITION_FIELD_ID).rawValue as String
                        )
                    }
        }

        log.info("Validation runner finished, [{}] validations successfully imported", numberActiveValidations)
    }
}