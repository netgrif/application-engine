package com.netgrif.application.engine.plugin

import com.netgrif.application.engine.adapter.spring.plugin.service.PluginService
import com.netgrif.application.engine.objects.plugin.domain.EntryPoint
import com.netgrif.application.engine.objects.plugin.domain.Method
import com.netgrif.application.engine.objects.plugin.domain.Plugin
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.plugin.meta.EntryPointMeta
import com.netgrif.application.engine.plugin.meta.PluginHolder
import com.netgrif.application.engine.plugin.meta.PluginMeta
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PluginInjector {

    @Autowired
    protected PluginService pluginService

    /**
     * Injects provided plugin into meta class of {@link ActionDelegate}
     *
     * @param plugin case of plugin to be injected
     * */
    void inject(Plugin plugin) {
        updateMetaClasses(plugin)
    }

    protected void updateMetaClasses(Plugin plugin) {
        MetaClass keyClassMeta = PluginHolder.metaClass
        MetaClass pluginMetaClass = PluginMeta.metaClass

        List<EntryPoint> entryPoints = plugin.entryPoints.values()

        entryPoints.each { ep ->
            MetaClass entryPointMetaClass = EntryPointMeta.metaClass
            List<Method> methods = ep.methods.values()

            methods.each { method->
                entryPointMetaClass[method.name] = { Serializable... args ->
                    return pluginService.call(plugin.identifier, ep.name, method.name, args)
                }
            }
            pluginMetaClass[ep.name] = new EntryPointMeta()
        }
        keyClassMeta[plugin.name] = new PluginMeta()
    }
}
