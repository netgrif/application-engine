package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.integration.plugin.injector.PluginInjector;
import com.netgrif.application.engine.integration.plugins.service.IPluginService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.core.workflow.domain.Case;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.netgrif.application.engine.integration.plugins.utils.PluginUtils.getPluginIdentifier;
import static com.netgrif.application.engine.integration.plugins.utils.PluginUtils.isPluginActive;

@Slf4j
@Component
@ConditionalOnProperty(
        value = "nae.plugin.enabled",
        havingValue = "true",
        matchIfMissing = true
)
class PluginRunner implements ApplicationEngineStartupRunner {

    @Autowired
    private ImportHelper helper;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private PluginInjector pluginInjector;

    private static final String PLUGIN_FILE_NAME = "engine-processes/plugin/plugin.xml";
    private static final String PLUGIN_PETRI_NET_IDENTIFIER = "plugin";

    private static final String ENTRY_POINT_FILE_NAME = "engine-processes/plugin/entry_point.xml";
    private static final String ENTRY_POINT_NET_IDENTIFIER = "entry_point";

    private static final String METHOD_FILE_NAME = "engine-processes/plugin/method.xml";
    private static final String METHOD_NET_IDENTIFIER = "method";

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
    }

    private void importPluginNets() {
        helper.upsertNet(PLUGIN_FILE_NAME, PLUGIN_PETRI_NET_IDENTIFIER);
        helper.upsertNet(ENTRY_POINT_FILE_NAME, ENTRY_POINT_NET_IDENTIFIER);
        helper.upsertNet(METHOD_FILE_NAME, METHOD_NET_IDENTIFIER);
    }
}
