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
}
