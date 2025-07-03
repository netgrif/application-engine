package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.adapter.spring.plugin.config.PluginRegistrationConfiguration;
import com.netgrif.application.engine.objects.plugin.domain.Plugin;
import com.netgrif.application.engine.plugin.PluginInjector;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RunnerOrder(85)
@ConditionalOnProperty(
        value = "nae.plugin.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PluginRunner implements ApplicationEngineStartupRunner {

    private final ApplicationContext applicationContext;
    private final PluginInjector pluginInjector;

    public PluginRunner(ApplicationContext applicationContext,
                        PluginInjector pluginInjector) {
        this.applicationContext = applicationContext;
        this.pluginInjector = pluginInjector;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Registering plugins.");
        Map<String, Plugin> pluginMap = new HashMap<>();

        applicationContext.getBeansOfType(PluginRegistrationConfiguration.class).forEach((key, config) -> {
            if (!pluginMap.containsKey(config.getPluginName())) {
                Plugin plugin = Plugin.builder()
                        .identifier(config.getPluginName())
                        .name(config.getPluginName())
                        .version(config.getVersion())
                        .description(StringUtils.EMPTY)
                        .entryPoints(config.getEntryPoints())
                        .build();
                pluginMap.put(config.getPluginName(), plugin);
            }
        });
        pluginMap.values().forEach(pluginInjector::inject);
    }

}

