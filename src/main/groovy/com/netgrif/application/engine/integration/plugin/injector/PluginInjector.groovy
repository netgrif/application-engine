package com.netgrif.application.engine.integration.plugin.injector

import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.integration.plugin.injector.meta.EntryPointMeta
import com.netgrif.application.engine.integration.plugin.injector.meta.PluginMeta
import com.netgrif.application.engine.integration.plugins.domain.Plugin
import com.netgrif.application.engine.integration.plugins.service.PluginService
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate

class PluginInjector {

    /**
     * Injects provided plugin into meta class of {@link ActionDelegate}
     *
     * @param plugin model of plugin to be injected
     * */
    static void inject(Plugin plugin) {
        updateMetaClasses(plugin, false)
    }

    /**
     * Removes provided plugin from the meta class of {@link ActionDelegate}.
     *
     * @param plugin model of plugin to be uninjected
     * */
    static void uninject(Plugin plugin) {
        updateMetaClasses(plugin, true)
    }

    protected static void updateMetaClasses(Plugin plugin, boolean isRemoval) {
        MetaClass actionDelegateMeta = ActionDelegate.metaClass
        MetaClass pluginMetaClass = PluginMeta.metaClass
        plugin.entryPoints.each {ep ->
            MetaClass entryPointMetaClass = EntryPointMeta.metaClass
            ep.value.methods.each { m ->
                if (isRemoval) {
                    entryPointMetaClass[m.value.name] = null
                } else {
                    entryPointMetaClass[m.value.name] << { Serializable... args ->
                        PluginService pluginService = ApplicationContextProvider.getBean("pluginService") as PluginService
                        return pluginService.call(plugin.identifier, ep.value.name, m.value.name, args)
                    }
                }
            }
            pluginMetaClass[ep.value.name] = isRemoval ? null : new EntryPointMeta()
        }
        actionDelegateMeta[plugin.name] = isRemoval ? null : new PluginMeta()
    }
}
