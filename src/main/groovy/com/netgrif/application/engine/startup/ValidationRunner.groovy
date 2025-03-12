package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.validations.interfaces.IValidationService
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.domain.QCase
import com.netgrif.application.engine.workflow.domain.repositories.CaseRepository
import com.querydsl.core.types.Predicate
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Slf4j
@Component
class ValidationRunner extends AbstractOrderedCommandLineRunner {

    private static final int PAGE_SIZE = 100
    public static final String VALIDATION_FILE_NAME = "engine-processes/validations/validation.xml"
    public static final String VALIDATION_PETRI_NET_IDENTIFIER = "validation"
    public static final String VALIDATION_ACTIVE_PLACE_ID = "active"
    public static final String VALIDATION_NAME_FIELD_ID = "name"
    public static final String VALIDATION_GROOVY_DEFINITION_FIELD_ID = "validation_definition_groovy"

    @Autowired
    private ImportHelper helper

    @Autowired
    private IUserService userService

    @Autowired
    private IValidationService validationService

    @Autowired
    private CaseRepository caseRepository

    @Override
    void run(String... strings) throws Exception {
        log.info("Starting validation runner")

        helper.upsertNet(VALIDATION_FILE_NAME, VALIDATION_PETRI_NET_IDENTIFIER)
        Predicate predicate = QCase.case$.processIdentifier.eq(VALIDATION_PETRI_NET_IDENTIFIER) & QCase.case$.activePlaces.get(VALIDATION_ACTIVE_PLACE_ID).isNotNull()
        long numberActiveValidations = caseRepository.count(predicate)
        int pageCount = (int) (numberActiveValidations / PAGE_SIZE) + 1
        pageCount.times { pageNum ->
            caseRepository.findAll(predicate, PageRequest.of(pageNum, PAGE_SIZE))
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