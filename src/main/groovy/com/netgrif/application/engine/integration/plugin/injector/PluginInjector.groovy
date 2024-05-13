package com.netgrif.application.engine.integration.plugin.injector

import com.netgrif.application.engine.configuration.ApplicationContextProvider
import com.netgrif.application.engine.integration.plugin.injector.meta.EntryPointMeta
import com.netgrif.application.engine.integration.plugin.injector.meta.PluginMeta
import com.netgrif.application.engine.integration.plugins.service.PluginService
import com.netgrif.application.engine.integration.plugins.utils.PluginUtils
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.workflow.domain.Case
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class PluginInjector {

    @Autowired
    protected IWorkflowService workflowService

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
        MetaClass actionDelegateMeta = ActionDelegate.metaClass
        MetaClass pluginMetaClass = PluginMeta.metaClass

        List<Case> entryPointCases = utils.getPluginEntryPoints(pluginCase)
        String pluginIdentifier = PluginUtils.getPluginIdentifier(pluginCase)
        String pluginName = PluginUtils.getPluginName(pluginCase)

        entryPointCases.each { epCase ->
            MetaClass entryPointMetaClass = EntryPointMeta.metaClass
            List<Case> methodCases = utils.getEntryPointMethods(epCase)
            String epName = PluginUtils.getEntryPointName(epCase)

            methodCases.each { methodCase ->
                String methodName = PluginUtils.getMethodName(methodCase)

                if (isRemoval) {
                    entryPointMetaClass[methodName] = null
                } else {
                    entryPointMetaClass[methodName] << { Serializable... args ->
                        PluginService pluginService = ApplicationContextProvider.getBean("pluginService") as PluginService
                        return pluginService.call(pluginIdentifier, epName, methodName, args)
                    }
                }
            }
            pluginMetaClass[epName] = isRemoval ? null : new EntryPointMeta()
        }
        actionDelegateMeta[pluginName] = isRemoval ? null : new PluginMeta()
    }
}
