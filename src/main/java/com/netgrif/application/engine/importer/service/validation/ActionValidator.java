package com.netgrif.application.engine.importer.service.validation;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ActionValidator extends ModelValidator implements IActionValidator {

    @Override
    public void validateAction(String action) {
        validateChangeFieldAbout(action);
    }

    private void validateChangeFieldAbout(String action) {
        if (action.matches("[\\s\\w\\W]*change [\\s\\w\\W]*? about[\\s\\w\\W]*")) {
            log.warn("Action [change field about] is deprecated.");
        }
    }
}