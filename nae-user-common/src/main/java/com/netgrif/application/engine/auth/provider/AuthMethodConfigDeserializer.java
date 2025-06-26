package com.netgrif.application.engine.auth.provider;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.netgrif.application.engine.objects.auth.provider.AuthMethodConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AuthMethodConfigDeserializer extends StdDeserializer<AuthMethodConfig<?>> {

    private final ProviderRegistry providerRegistry;

    @Autowired
    public AuthMethodConfigDeserializer(ProviderRegistry providerRegistry) {
        super(AuthMethodConfig.class);
        this.providerRegistry = providerRegistry;
    }

    @Override
    public AuthMethodConfig<?> deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JacksonException {
        JsonNode node = jp.getCodec().readTree(jp);

        JsonNode realmId = node.get("realmId");
        if (realmId == null) {
            throw new IllegalArgumentException("Missing required field: realmID");
        }
        String realmID = realmId.asText();

        JsonNode typeNode = node.get("type");
        if (typeNode == null) {
            throw new IllegalArgumentException("Missing required field: type");
        }
        String type = typeNode.asText();

        AuthMethodConfig<Object> config = new AuthMethodConfig<>();
        JsonNode idNode = node.get("id");
        if (idNode != null) {
            config.setId(idNode.asText());
        }

        JsonNode nameNode = node.get("name");
        if (nameNode != null) {
            config.setName(nameNode.asText());
        }

        JsonNode enabledNode = node.get("enabled");
        if (enabledNode != null) {
            config.setEnabled(enabledNode.asBoolean());
        }

        JsonNode orderNode = node.get("order");
        if (orderNode != null) {
            config.setOrder(orderNode.asInt());
        }

        config.setType(type);
        config.setRealmId(realmID);

        JsonNode configNode = node.get("configuration");
        if (configNode == null) {
            throw new IllegalArgumentException("Missing required field: configuration");
        }

        Class<?> configClass = providerRegistry.getConfigClass(type);

        if (configClass == null) {
            throw new IllegalArgumentException("No provider registered for type: " + type);
        }

        Object configuration = jp.getCodec().treeToValue(configNode, configClass);
        config.setConfiguration(configuration);

        return config;
    }
}
