package com.netgrif.workflow.configuration.keycloak;

import com.netgrif.workflow.oauth.service.KeycloakUserResourceService;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnExpression("${nae.oauth.keycloak}")
public class KeycloakConfiguration {

    @Value("${security.oauth2.client.clientId}")
    private String clientId;

    @Value("${security.oauth2.client.clientSecret}")
    private String clientSecret;

    @Value("${security.oauth2.client.realm}")
    private String realm;

    @Value("${security.oauth2.client.serverUrl}")
    private String serverUrl;

    @Value("${security.oauth2.client.username}")
    private String username;

    @Value("${security.oauth2.client.password}")
    private String password;

    @Bean
    public IRemoteUserResourceService<?> remoteUserResourceService() {
        return new KeycloakUserResourceService();
    }

    @Bean
    public Keycloak keycloak() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .username(username)
                .password(password)
                .build();
        return keycloak;
    }

}
