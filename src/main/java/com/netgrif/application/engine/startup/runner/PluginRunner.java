package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.integration.plugin.injector.PluginInjector;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.pluginlibrary.core.domain.Plugin;
import com.netgrif.pluginlibrary.core.service.EntryPointLoaderService;
import com.netgrif.pluginlibrary.core.service.PluginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.netgrif.application.engine.integration.plugins.utils.PluginUtils.getPluginIdentifier;
import static com.netgrif.application.engine.integration.plugins.utils.PluginUtils.isPluginActive;

@Slf4j
@Component
@RunnerOrder(85)
@ConditionalOnProperty(
        value = "nae.plugin.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PluginRunner implements ApplicationEngineStartupRunner {

    private final ImportHelper helper;

    private final PluginService pluginService;

    private final PluginInjector pluginInjector;

    private final EntryPointLoaderService entryPointLoaderService;

    private static final String PLUGIN_FILE_NAME = "engine-processes/plugin/plugin.xml";
    private static final String PLUGIN_PETRI_NET_IDENTIFIER = "plugin";

    private static final String ENTRY_POINT_FILE_NAME = "engine-processes/plugin/entry_point.xml";
    private static final String ENTRY_POINT_NET_IDENTIFIER = "entry_point";

    private static final String METHOD_FILE_NAME = "engine-processes/plugin/method.xml";
    private static final String METHOD_NET_IDENTIFIER = "method";

    public PluginRunner(ImportHelper helper,
                        PluginService pluginService,
                        PluginInjector pluginInjector,
                        @Autowired(required = false) EntryPointLoaderService entryPointLoaderService) {
        this.helper = helper;
        this.pluginService = pluginService;
        this.pluginInjector = pluginInjector;
        this.entryPointLoaderService = entryPointLoaderService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        importPluginNets();

        List<Case> plugins = pluginService.findAll();

        log.info("Re-injecting ${plugins.size()} plugins from database into memory.");
        for (Case plugin : plugins) {
            if (isPluginActive(plugin)) {
                pluginInjector.inject(plugin);
            } else {
                log.warn("Plugin with identifier [{}] is disabled and will not be injected.", getPluginIdentifier(plugin));
            }
        }

        log.info("Registering new plugins.");
        Map<String, Plugin> pluginMap = new HashMap<>();

        if (entryPointLoaderService != null) {
            entryPointLoaderService.getAll().forEach(entryPoint -> {
                if (!pluginMap.containsKey(entryPoint.getPluginName())) {
                    Plugin plugin = Plugin.builder()
                            .identifier(entryPoint.getPluginName())
                            .name(entryPoint.getPluginName())
                            .version("0.0.1")
                            .url(StringUtils.EMPTY)
                            .port(0)
                            .description(StringUtils.EMPTY)
                            .entryPoints(new HashMap<>())
                            .build();
                    pluginMap.put(entryPoint.getPluginName(), plugin);
                }
                pluginMap.get(entryPoint.getPluginName()).getEntryPoints().put(entryPoint.getName(), entryPoint);
            });
            pluginMap.values().forEach(pluginService::register);
            log.info("All new plugins are registered.");
        }
    }

    private void importPluginNets() {
        helper.upsertNet(PLUGIN_FILE_NAME, PLUGIN_PETRI_NET_IDENTIFIER);
        helper.upsertNet(ENTRY_POINT_FILE_NAME, ENTRY_POINT_NET_IDENTIFIER);
        helper.upsertNet(METHOD_FILE_NAME, METHOD_NET_IDENTIFIER);
    }
}
