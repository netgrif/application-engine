package com.netgrif.workflow.configuration.security.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.security.jwt")
public class JwtProperties {

    /**
     * Defines the validity duration for a token in milliseconds, then expiration dateTime is
     * counted using "System.currentTimeMillis() + this.expiration"
     * */
    private long expiration = 900000;

    /**
     * Defines path to a file that contains generated private key with certificate
     * */
    private String privateKey = "src/main/resources/certificates/private.der";

    /**
     * Defines which algorithm is used when generating JWT token
     * */
    private String algorithm = "RSA";
}
