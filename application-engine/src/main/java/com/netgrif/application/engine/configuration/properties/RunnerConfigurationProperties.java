package com.netgrif.application.engine.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;


/**
 * Configuration properties for the Netgrif engine runner.
 * Provides properties to customize the behavior of different runner components.
 */
@Data
@ConfigurationProperties(prefix = "netgrif.engine.runner")
public class RunnerConfigurationProperties {

    /**
     * Configuration for the expression runner, including cache size.
     */
    private ExpressionRunnerProperties expressionRunner = new ExpressionRunnerProperties();

    /**
     * Configuration for the field runner, including action, function, and namespace cache sizes.
     */
    private FieldRunnerProperties fieldRunner = new FieldRunnerProperties();

    /**
     * Configuration specific to the expression runner component.
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.runner.expression-runner")
    public static class ExpressionRunnerProperties {

        /**
         * The size of the cache used for managing expression runner objects.
         */
        private int cacheSize = 200;

    }

    /**
     * Configuration specific to the field runner component.
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.runner.field-runner")
    public static class FieldRunnerProperties {

        /**
         * The size of the cache used for handling field runner actions.
         */
        private int actionCacheSize = 500;

        /**
         * The size of the cache used for managing field runner functions.
         */
        private int functionsCacheSize = 500;

        /**
         * The size of the cache used for managing global Petri net functions.
         */
        private int globalFunctionsCacheSize = 500;
    }

    /**
     * Configuration specific to the application runner component.
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.runner.application-runner")
    public static class ApplicationRunnerProperties {

        /**
         * Allow a runner to be executed multiple times in the runner execution chain.
         * It can be applied to runner annotations {@code RunnerOrder} and {@code ReplaceRunner}.
         * If set to {@code false} (default value), only the first runner order annotation is considered.
         */
        private boolean enableMultipleExecution = false;

        /**
         * If true, unresolved runner order annotations will cause the runner
         * to be executed at the end of the runner execution chain.
         */
        private boolean runUnresolved = false;
    }
}
