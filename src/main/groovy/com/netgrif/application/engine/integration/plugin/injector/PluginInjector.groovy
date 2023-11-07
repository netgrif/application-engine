package com.netgrif.application.engine.integration.plugin.injector

import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.integration.plugins.domain.Plugin
import com.netgrif.application.engine.integration.plugins.service.PluginService
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Component

@Component
@Slf4j
class PluginInjector {

    void inject(Plugin plugin) {
        MetaClass actionDelegateMeta = ActionDelegate.metaClass
        PluginMeta pluginMeta = new PluginMeta()
        plugin.entryPoints.each {ep ->
            EntryPointMeta entryPointMeta = new EntryPointMeta()
            ep.value.methods.each { m ->
                entryPointMeta.metaClass[m.value.name] << { Serializable... args ->
                    PluginService pluginService = ApplicationContextProvider.getBean("pluginService")
                    return pluginService.call(plugin.identifier, ep.value.identifier, m.value.name, args)}
            }
            pluginMeta.metaClass[ep.value.identifier] = entryPointMeta
        }
        actionDelegateMeta[plugin.identifier] = pluginMeta
        log.info("Injected plugin into ActionDelegate")
    }
}
