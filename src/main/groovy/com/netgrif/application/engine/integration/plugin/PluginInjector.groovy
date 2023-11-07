package com.netgrif.application.engine.integration.plugin

import com.netgrif.application.engine.integration.plugins.domain.Plugin
import com.netgrif.application.engine.integration.plugins.service.PluginService
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class PluginInjector {

    @Autowired
    private final ActionDelegate actionDelegate

    void inject(Plugin plugin, PluginService pluginService) {
        actionDelegate.metaClass[plugin.identifier] = new Object()
        plugin.entryPoints.each {ep ->
            actionDelegate.metaClass[plugin.identifier].metaClass[ep.value.identifier] = new Object()
            ep.value.methods.each { m ->
                actionDelegate.metaClass[plugin.identifier].metaClass[ep.value.identifier].metaClass[m.value.name] << { Serializable... args -> pluginService.call(plugin.identifier, ep.value.identifier, m.value.name, args)}
            }
        }
    }
}
