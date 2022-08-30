package com.netgrif.application.engine.configuration.authentication.providers;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Netgrif Security
 */
@Data
@Component
@ConfigurationProperties(prefix = "nae.security")
public class NaeAuthProperties {

    /**
     * Netgrif Community providers:
     *
     * NetgrifBasicAuthenticationProvider, NetgrifLdapAuthenticationProvider
     */
    private String[] providers;

    private String[] serverPatterns;

    private String[] staticPatterns = {"/**/favicon.ico", "/favicon.ico", "/**/manifest.json", "/manifest.json", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"};

    private String[] anonymousExceptions;

}
