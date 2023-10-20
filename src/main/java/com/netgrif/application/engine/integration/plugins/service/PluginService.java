package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.application.engine.integration.plugins.domain.Plugin;
import com.netgrif.application.engine.integration.plugins.repository.PluginRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public final class PluginService implements IPluginService {
    private final PluginRepository pluginRepository;

    @Override
    public void register(Plugin plugin) {
        Plugin existingPlugin = pluginRepository.findByIdentifier(plugin.getIdentifier());
        if (existingPlugin != null) {
            throw new IllegalArgumentException("Plugin with identifier \"" + plugin.getIdentifier() + "\" cannot be registered. Plugin with this identifier has already been registered.");
        }
        pluginRepository.save(plugin);
        log.info("Plugin with identifier \"" + plugin.getIdentifier() + "\" was registered.");
    }

    @Override
    public void unregister(String identifier) {
        Plugin existingPlugin = pluginRepository.findByIdentifier(identifier);
        if (existingPlugin == null) {
            throw new IllegalArgumentException("Plugin with identifier \"" + identifier + "\" cannot be unregistered. Plugin with this identifier does not exist.");
        }
        pluginRepository.delete(existingPlugin);
        log.info("Plugin with identifier \"" + identifier + "\" was unregistered.");
    }


}
