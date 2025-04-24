package com.netgrif.application.engine.auth.provider;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class ProviderRegistry {

    protected final Map<String, Class<? extends AbstractAuthConfig>> configClasses = new HashMap<>();

    protected final Map<String, AuthMethodProvider<?>> providers = new HashMap<>();

    public void registerProvider(String type, AuthMethodProvider<?> provider) {
        providers.put(type.toLowerCase(), provider);
        configClasses.put(type.toLowerCase(), provider.getConfigClass());
        log.info("Registered provider for type: " + type);
    }

    public Class<? extends AbstractAuthConfig> getConfigClass(String type) {
        return configClasses.get(type.toLowerCase());
    }

    public AuthMethodProvider<?> getProvider(String type) {
        return providers.get(type.toLowerCase());
    }

    public Map<String, Class<? extends AbstractAuthConfig>> getConfigClasses() {
        return Collections.unmodifiableMap(configClasses);
    }

    public Map<String, AuthMethodProvider<?>> getProviders() {
        return Collections.unmodifiableMap(providers);
    }

    public Set<String> getRegisteredTypes() {
        return Collections.unmodifiableSet(providers.keySet());
    }

    public Map<String, Map<String, Object>> getProvidersWithConfig() {
        Map<String, Map<String, Object>> providersWithConfig = new HashMap<>();
        for (Map.Entry<String, AuthMethodProvider<?>> entry : providers.entrySet()) {
            String type = entry.getKey();
            AuthMethodProvider<?> provider = entry.getValue();
            Class<? extends AbstractAuthConfig> configClass = configClasses.get(type);
            Map<String, Object> providerDetails = new HashMap<>();
            providerDetails.put("provider", provider);
            providerDetails.put("configClass", configClass);
            providersWithConfig.put(type, providerDetails);
        }
        return providersWithConfig;
    }
}
