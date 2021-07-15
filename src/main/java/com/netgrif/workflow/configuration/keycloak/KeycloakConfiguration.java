package com.netgrif.workflow.configuration.keycloak;

import com.netgrif.workflow.oauth.service.KeycloakGroupResourceService;
import com.netgrif.workflow.oauth.service.KeycloakUserResourceService;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteGroupResourceService;
import com.netgrif.workflow.oauth.service.interfaces.IRemoteUserResourceService;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
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

    @Value("${nae.oauth.keycloak.clientId}")
    private String clientId;

    @Value("${nae.oauth.keycloak.clientSecret}")
    private String clientSecret;

    @Value("${security.oauth2.client.realm}")
    private String realm;

    @Value("${security.oauth2.client.serverUrl}")
    private String serverUrl;

    @Value("${security.oauth2.client.pool-size:10}")
    private int poolSize;

    @Bean
    public IRemoteUserResourceService<?> remoteUserResourceService() {
        return new KeycloakUserResourceService();
    }

    @Bean
    public IRemoteGroupResourceService<?, ?> remoteGroupResourceService() {
        return new KeycloakGroupResourceService();
    }

    @Bean
    public Keycloak keycloak() {
        Keycloak keycloak = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realm)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(clientId)
                .clientSecret(clientSecret)
                .resteasyClient(new ResteasyClientBuilder().connectionPoolSize(poolSize).build())
                .build();
        return keycloak;
    }

}
