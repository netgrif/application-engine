package com.netgrif.application.engine.startup


import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest
import com.netgrif.application.engine.validations.interfaces.IValidationService
import com.netgrif.application.engine.workflow.domain.Case
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Component

@Slf4j
@Component
@Profile("!test")
@CompileStatic
class ValidationRunner extends AbstractOrderedCommandLineRunner {

    private static final int PAGE_SIZE = 100

    @Autowired
    private IUserService userService

    @Autowired
    private IValidationService validationService

    @Autowired
    private IElasticCaseService elasticCaseService

    @Override
    void run(String... strings) throws Exception {
        log.info("Starting validation runner")
        CaseSearchRequest request = new CaseSearchRequest()
        request.query = "processIdentifier:validation AND dataSet.is_active.value:true"
        int pageCount = (int) (elasticCaseService.count([request], userService.loggedOrSystem.transformToLoggedUser(), LocaleContextHolder.locale, false) / PAGE_SIZE)
        pageCount.times {
            elasticCaseService.search([request], userService.loggedOrSystem.transformToLoggedUser(), PageRequest.of(it, PAGE_SIZE), LocaleContextHolder.locale, false)
                    .getContent()
                    .each { Case validationCase ->
                        validationService.registerValidation(validationCase.getDataSet().get("name").getValue() as String, validationCase.getDataSet().get("validation_definition_groovy").getValue() as String)
                    }
        }
    }
}