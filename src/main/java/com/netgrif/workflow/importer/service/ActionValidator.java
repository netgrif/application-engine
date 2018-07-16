package com.netgrif.workflow.importer.service;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class ActionValidator implements IActionValidator {

    private static final Logger log = Logger.getLogger(ActionValidator.class);

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