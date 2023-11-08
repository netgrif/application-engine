package com.netgrif.application.engine.integration.plugin.injector

import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.integration.plugin.injector.meta.EntryPointMeta
import com.netgrif.application.engine.integration.plugin.injector.meta.PluginMeta
import com.netgrif.application.engine.integration.plugins.domain.Plugin
import com.netgrif.application.engine.integration.plugins.service.PluginService
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate


class PluginInjector {

    static void inject(Plugin plugin) {
        MetaClass actionDelegateMeta = ActionDelegate.metaClass
        MetaClass pluginMetaClass = PluginMeta.metaClass
        plugin.entryPoints.each {ep ->
            MetaClass entryPointMetaClass = EntryPointMeta.metaClass
            ep.value.methods.each { m ->
                entryPointMetaClass[m.value.name] << { Serializable... args ->
                    PluginService pluginService = ApplicationContextProvider.getBean("pluginService") as PluginService
                    return pluginService.call(plugin.identifier, ep.value.identifier, m.value.name, args)}
            }
            pluginMetaClass[ep.value.identifier] = new EntryPointMeta()
        }
        actionDelegateMeta[plugin.identifier] = new PluginMeta()
    }
}
