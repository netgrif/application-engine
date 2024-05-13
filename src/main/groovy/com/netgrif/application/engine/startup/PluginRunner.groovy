package com.netgrif.application.engine.startup

import com.netgrif.application.engine.integration.plugin.injector.PluginInjector
import com.netgrif.application.engine.integration.plugins.service.IPluginService
import com.netgrif.application.engine.workflow.domain.Case
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class PluginRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ImportHelper helper

    @Autowired
    private IPluginService pluginService

    @Autowired
    private PluginInjector pluginInjector

    private static final String PLUGIN_FILE_NAME = "engine-processes/plugin/plugin.xml"
    private static final String PLUGIN_PETRI_NET_IDENTIFIER = "plugin"

    private static final String ENTRY_POINT_FILE_NAME = "engine-processes/plugin/entry_point.xml"
    private static final String ENTRY_POINT_NET_IDENTIFIER = "entry_point"

    private static final String METHOD_FILE_NAME = "engine-processes/plugin/method.xml"
    private static final String METHOD_NET_IDENTIFIER = "method"

    @Override
    void run(String... args) throws Exception {
        importPluginNets()

        List<Case> plugins = pluginService.findAll()
        plugins.size()

        log.info("Re-injecting ${plugins.size()} plugins from database into memory.")
        plugins.each { plugin ->
            pluginInjector.inject(plugin)
        }
    }

    private void importPluginNets() {
        helper.upsertNet(PLUGIN_FILE_NAME, PLUGIN_PETRI_NET_IDENTIFIER)
        helper.upsertNet(ENTRY_POINT_FILE_NAME, ENTRY_POINT_NET_IDENTIFIER)
        helper.upsertNet(METHOD_FILE_NAME, METHOD_NET_IDENTIFIER)
    }
}
