package com.netgrif.application.engine.importer.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ActionValidator implements IActionValidator {

    private static final Logger log = LoggerFactory.getLogger(ActionValidator.class);

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