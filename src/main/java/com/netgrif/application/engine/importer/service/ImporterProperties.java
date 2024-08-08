package com.netgrif.application.engine.importer.service;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "nae.importer")
public class ImporterProperties {

    /**
     * Whether a process should be validated and checked for deprecated attributes and code.
     */
    private boolean validateProcess = true;

    /**
     * Whether actions should be evaluated ahead of time during the import. Turn off for faster import in exchange for
     * a slower on-demand evaluation of actions.
     */
    private boolean evaluateActions = true;

    /**
     * Whether functions should be evaluated ahead of time during the import. Turn off for faster import in exchange for
     * a slower on-demand evaluation of functions.
     */
    private boolean evaluateFunctions = true;

    /**
     * Process identifier of the Object process used as default parent process.
     */
    private String objectProcess;
}
