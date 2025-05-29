package com.netgrif.application.engine.importer.service.validation;

import com.netgrif.application.engine.petrinet.domain.Process;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "nae.importer", value = "validate-process", havingValue = "false")
public class SkipProcessValidator implements IProcessValidator {

    @Override
    public void validate(Process process) {
        log.info("Skipping process validation");
    }
}
