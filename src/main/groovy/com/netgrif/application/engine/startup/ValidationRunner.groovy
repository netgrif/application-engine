package com.netgrif.application.engine.startup

import com.netgrif.application.engine.validations.interfaces.IValidationService
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Slf4j
@Component
@Profile("!test")
@CompileStatic
class ValidationRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IValidationService service

    @Override
    void run(String... strings) throws Exception {
        log.info("Starting validation runner")
    }
}