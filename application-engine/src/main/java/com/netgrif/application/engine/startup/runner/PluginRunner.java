package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.adapter.spring.plugin.service.EntryPointLoaderService;
import com.netgrif.application.engine.objects.plugin.domain.Plugin;
import com.netgrif.application.engine.plugin.PluginInjector;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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

    private final EntryPointLoaderService entryPointLoaderService;

    private final PluginInjector pluginInjector;

    public PluginRunner(@Autowired(required = false) EntryPointLoaderService entryPointLoaderService,
                        PluginInjector pluginInjector) {
        this.entryPointLoaderService = entryPointLoaderService;
        this.pluginInjector = pluginInjector;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Registering plugins.");
        Map<String, Plugin> pluginMap = new HashMap<>();

        entryPointLoaderService.getAll().forEach(entryPoint -> {
            if (!pluginMap.containsKey(entryPoint.getPluginName())) {
                Plugin plugin = Plugin.builder()
                        .identifier(entryPoint.getPluginName())
                        .name(entryPoint.getPluginName())
                        .version("0.0.1")
                        .description(StringUtils.EMPTY)
                        .entryPoints(new HashMap<>())
                        .build();
                pluginMap.put(entryPoint.getPluginName(), plugin);
            }
            pluginMap.get(entryPoint.getPluginName()).getEntryPoints().put(entryPoint.getName(), entryPoint);
        });
        pluginMap.values().forEach(pluginInjector::inject);
    }

}

