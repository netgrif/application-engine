package com.netgrif.application.engine.startup

import com.netgrif.application.engine.integration.plugin.injector.PluginInjector
import com.netgrif.application.engine.integration.plugins.domain.Plugin
import com.netgrif.application.engine.integration.plugins.service.IPluginService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class PluginRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IPluginService pluginService

    @Override
    void run(String... args) throws Exception {
        List<Plugin> plugins = pluginService.findAll()
        plugins.size()

        log.info("Re-injecting ${plugins.size()} plugins from database into memory.")
        plugins.each { plugin ->
            PluginInjector.inject(plugin)
        }
    }
}
