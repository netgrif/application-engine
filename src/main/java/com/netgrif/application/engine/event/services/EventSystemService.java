package com.netgrif.application.engine.event.services;

//import com.netgrif.application.engine.event.services.interfaces.IEventSystemService;
//import com.netgrif.application.engine.integration.plugins.services.interfaces.IPluginService;
//import events.IDispatcher;
//import org.pf4j.Plugin;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import wrapper.NaePlugin;

/**
 * Service for managing and handling Spring Event objects inside NAE
 * */
//@Service
//public class EventSystemService implements IEventSystemService {
//
//    @Autowired
//    private IPluginService pluginService;
//
//    /**
//     * Registers dispatcher object in the required plugin, then calls the subscribers to subscribe to this dispatcher.
//     * @param dispatcher the Dispatcher object that is being registered
//     * */
//    @Override
//    public void registerDispatcher(IDispatcher dispatcher) {
//        this.pluginService.getAllPlugin().stream()
//                .filter(plugin -> plugin.getEventManager().getRequiredDispatchers().contains(dispatcher.getId()))
//                .forEach(plugin -> {
//                    plugin.getEventManager().addDispatcher(dispatcher);
//                    plugin.getEventManager().registerSubscribers(dispatcher);
//                });
//    }
//}
