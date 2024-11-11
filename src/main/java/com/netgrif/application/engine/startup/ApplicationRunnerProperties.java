package com.netgrif.application.engine.startup;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "nae.runner")
public class ApplicationRunnerProperties {

    /**
     * Allow a runner to be executed multiple times in the runner execution chain.
     * It can be applied to runner annotations {@code RunnerOrder} and {@code ReplaceRunner}
     * If set to {@code false} (default value) only first runner order annotation is considered.
     */
    private boolean enableMultipleExecution = false;

    /**
     * If true runner which order cannot be resolved will be executed at the end of the runner execution chain.
     */
    private boolean runUnresolved = false;

}
