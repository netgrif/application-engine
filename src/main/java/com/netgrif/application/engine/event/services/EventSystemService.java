package com.netgrif.application.engine.event.services;

import com.netgrif.application.engine.event.services.interfaces.IEventSystemService;
import com.netgrif.application.engine.integration.plugins.services.interfaces.IPluginService;
import events.IDispatcher;
import org.pf4j.Plugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import wrapper.NaePlugin;

/**
 * Service for managing and handling Spring Event objects inside NAE
 * */
@Service
public class EventSystemService implements IEventSystemService {

    @Autowired
    private IPluginService pluginService;

    /**
     * Registers dispatcher object in the required plugin, then calls the subscribers to subscribe to this dispatcher.
     * @param dispatcher the Dispatcher object that is being registered
     * */
    @Override
    public void registerDispatcher(IDispatcher dispatcher) {
        if (dispatcher.getRequiredPluginIds() != null) {
            dispatcher.getRequiredPluginIds().forEach(pluginId -> {
                Plugin plugin = this.pluginService.getPlugin(pluginId);
                if (plugin instanceof NaePlugin) {
                    NaePlugin naePlugin = (NaePlugin) plugin;
                    naePlugin.getEventManager().addDispatcher(dispatcher);
                    naePlugin.getEventManager().registerSubscribers(dispatcher);
                }
            });
        }
    }
}
