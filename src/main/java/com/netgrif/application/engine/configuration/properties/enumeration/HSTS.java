package com.netgrif.application.engine.configuration.properties.enumeration;

import lombok.Data;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * The HTTP Strict-Transport-Security response header (often abbreviated as HSTS)
 * informs browsers that the site should only be accessed using HTTPS,
 * and that any future attempts to access it using HTTP should automatically be converted to HTTPS.
 * <p>
 * More <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Strict-Transport-Security">Info</a>
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "nae.server.security.headers.hsts")
public class HSTS {

    private boolean enable;

    /**
     * The time, in seconds, that the browser should remember that a site is only to be accessed using HTTPS.
     * <p>
     * Default value: 31536000
     */
    private long maxAge = 31536000;

    /**
     * If this optional parameter is specified, this rule applies to all of the site's subdomains as well.
     */
    private boolean includeSubDomains;

    /**
     * See Preloading Strict Transport Security for details.
     * When using preload, the max-age directive must be at least 31536000 (1 year),
     * and the includeSubDomains directive must be present. Not part of the specification.
     * <p>
     */
    private boolean preload;
}
