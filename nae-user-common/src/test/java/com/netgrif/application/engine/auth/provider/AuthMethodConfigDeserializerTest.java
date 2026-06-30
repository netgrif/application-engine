package com.netgrif.application.engine.auth.provider;

import com.netgrif.application.engine.objects.auth.provider.AuthMethod;
import com.netgrif.application.engine.objects.auth.provider.AuthMethodConfig;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthMethodConfigDeserializerTest {

    private final ObjectMapper mapper = JsonMapper.builder()
            .addModule(new SimpleModule().addDeserializer(
                    AuthMethodConfig.class,
                    new AuthMethodConfigDeserializer(registry())))
            .build();

    @Test
    void deserializesRegisteredProviderConfiguration() throws Exception {
        AuthMethodConfig<?> config = mapper.readValue(
                """
                {
                  "id": "auth-1",
                  "name": "Basic login",
                  "enabled": false,
                  "description": "Login through basic auth",
                  "order": 3,
                  "realmId": "default",
                  "type": "basic",
                  "configuration": {
                    "endpoint": "/login",
                    "allowUserCreation": false
                  }
                }
                """,
                AuthMethodConfig.class
        );

        assertEquals("auth-1", config.getId());
        assertEquals("Basic login", config.getName());
        assertFalse(config.isEnabled());
        assertEquals("Login through basic auth", config.getDescription());
        assertEquals(3, config.getOrder());
        assertEquals("default", config.getRealmId());
        assertEquals("basic", config.getType());
        TestAuthConfig providerConfig = assertInstanceOf(TestAuthConfig.class, config.getConfiguration());
        assertEquals("/login", providerConfig.endpoint);
        assertFalse(providerConfig.isAllowUserCreation());
    }

    @Test
    void rejectsMissingRealmId() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                mapper.readValue("{\"type\":\"basic\",\"configuration\":{}}", AuthMethodConfig.class)
        );

        assertEquals("Missing required field: realmID", exception.getMessage());
    }

    @Test
    void rejectsUnknownProviderType() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
                mapper.readValue("{\"realmId\":\"default\",\"type\":\"unknown\",\"configuration\":{}}", AuthMethodConfig.class)
        );

        assertEquals("No provider registered for type: unknown", exception.getMessage());
    }

    private ProviderRegistry registry() {
        ProviderRegistry registry = new ProviderRegistry();
        registry.registerProvider("basic", new TestAuthMethodProvider());
        return registry;
    }

    static class TestAuthConfig extends AbstractAuthConfig {
        public String endpoint;

        @Override
        public AbstractAuthConfig of(Map<String, Object> map) {
            endpoint = (String) map.get("endpoint");
            return this;
        }
    }

    static class TestAuthMethodProvider implements AuthMethodProvider<TestAuthConfig> {

        @Override
        public String getProviderType() {
            return "basic";
        }

        @Override
        public Class<TestAuthConfig> getConfigClass() {
            return TestAuthConfig.class;
        }

        @Override
        public Class<? extends AuthMethod<TestAuthConfig>> getAuthMethodClass() {
            return null;
        }
    }
}
