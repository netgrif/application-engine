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

/**
 * Component responsible for injecting plugin entry points and methods into
 * the application's meta-class system at runtime. Uses Groovy's metaClass
 * mechanism to dynamically add closures that delegate calls to PluginService.
 *
 * @see ActionDelegate
 * @see PluginService
 */
@Component
class PluginInjector {

    /**
     * Service used to invoke methods on the injected plugins.
     */
    @Autowired
    protected PluginService pluginService

    /**
     * Injects the provided plugin into the application's meta-class system.
     * This will allow calls to plugin entry points and methods via dynamic
     * properties on {@link PluginHolder}.
     *
     * @param plugin the Plugin instance to be injected
     */
    void inject(Plugin plugin) {
        updateMetaClasses(plugin)
    }

    /**
     * Updates meta-class definitions for the given plugin by registering
     * each entry point and its methods as closures. Each generated closure
     * delegates invocation to the {@link PluginService}.
     *
     * @param plugin the Plugin instance whose entry points and methods
     *               are to be exposed dynamically
     */
    protected void updateMetaClasses(Plugin plugin) {
        def pluginMeta = new PluginMeta()

        plugin.entryPoints.values().each { EntryPoint ep ->
            def epMeta = new EntryPointMeta()

            ep.methods.values().each { Method method ->
                /**
                 * Dynamically generated method closure for entry point invocation.
                 *
                 * @param args variable-length list of Serializable arguments
                 * @return the result returned by PluginService.call(...)
                 */
                epMeta.metaClass."${method.name}" = { Serializable... args ->
                    pluginService.call(plugin.identifier, ep.name, method.name, args)
                }
            }

            pluginMeta.metaClass."${ep.name}" = epMeta
        }

        PluginHolder.metaClass."${plugin.name}" = pluginMeta
    }
}
