package com.netgrif.application.engine.integration.plugin.injector

import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.integration.plugin.injector.meta.EntryPointMeta
import com.netgrif.application.engine.integration.plugin.injector.meta.PluginMeta
import com.netgrif.application.engine.integration.plugins.service.PluginServiceImpl
import com.netgrif.application.engine.integration.plugins.utils.PluginUtils
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.core.workflow.domain.Case
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PluginInjector {

    @Autowired
    protected PluginUtils utils

    /**
     * Injects provided plugin into meta class of {@link ActionDelegate}
     *
     * @param plugin case of plugin to be injected
     * */
    void inject(Case plugin) {
        updateMetaClasses(plugin, false)
    }

    /**
     * Removes provided plugin from the meta class of {@link ActionDelegate}.
     *
     * @param plugin case of plugin to be uninjected
     * */
    void uninject(Case plugin) {
        updateMetaClasses(plugin, true)
    }

    protected void updateMetaClasses(Case pluginCase, boolean isRemoval) {
        MetaClass keyClassMeta = PluginHolder.metaClass
        MetaClass pluginMetaClass = PluginMeta.metaClass

        List<Case> entryPointCases = utils.getPluginEntryPoints(pluginCase)

        String pluginName = PluginUtils.getPluginName(pluginCase)
        String pluginUrl = PluginUtils.getPluginUrl(pluginCase)
        int pluginPort = PluginUtils.getPluginPort(pluginCase)

        entryPointCases.each { epCase ->
            MetaClass entryPointMetaClass = EntryPointMeta.metaClass
            List<Case> methodCases = utils.getEntryPointMethods(epCase)
            String epName = PluginUtils.getEntryPointName(epCase)

            methodCases.each { methodCase ->
                String methodName = PluginUtils.getMethodName(methodCase)
                if (isRemoval) {
                    entryPointMetaClass[methodName] = null
                } else {
                    entryPointMetaClass[methodName] = { Serializable... args ->
                        PluginServiceImpl pluginService = ApplicationContextProvider.getBean("pluginService") as PluginServiceImpl
                        return pluginService.call(pluginUrl, pluginPort, epName, methodName, args)
                    }
                }
            }
            pluginMetaClass[epName] = isRemoval ? null : new EntryPointMeta()
        }
        keyClassMeta[pluginName] = isRemoval ? null : new PluginMeta()
    }
}
