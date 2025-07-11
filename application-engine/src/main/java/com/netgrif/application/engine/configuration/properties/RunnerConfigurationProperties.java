package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.runner")
public class RunnerConfigurationProperties {
    private ExpressionRunnerProperties expressionRunner = new ExpressionRunnerProperties();
    private FieldRunnerProperties fieldRunner = new FieldRunnerProperties();

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.runner.expression-runner")
    public static class ExpressionRunnerProperties {
        private int cacheSize = 200;

    }

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.runner.field-runner")
    public static class FieldRunnerProperties {
        private int actionCacheSize = 500;
        private int functionsCacheSize = 500;
        private int namespaceCacheSize = 500;
    }

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.runner.application-runner")
    public static class ApplicationRunnerProperties {
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
}
