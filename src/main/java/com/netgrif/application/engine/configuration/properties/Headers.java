package com.netgrif.application.engine.configuration.properties;

import com.netgrif.application.engine.configuration.properties.enumeration.HSTS;
import com.netgrif.application.engine.configuration.properties.enumeration.XFrameOptionsMode;
import com.netgrif.application.engine.configuration.properties.enumeration.XXSSProtection;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "nae.server.security.headers")
public class Headers {

    /**
     * Allowed HOST in HTTP header
     */
    private List<String> hostAllowed;

    /**
     * The X-Frame-Options HTTP response header can be used to indicate whether or not a browser should be
     * allowed to render a page in a <frame>, <iframe>, <embed> or <object>.
     * Sites can use this to avoid click-jacking attacks, by ensuring that their content is not embedded into other sites.
     * <p>
     * More <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-Frame-Options">Info</a>
     */
    private XFrameOptionsMode xFrameOptions = XFrameOptionsMode.DISABLE;

    /**
     * The HTTP X-XSS-Protection response header is a feature of Internet Explorer, Chrome and Safari
     * that stops pages from loading when they detect reflected cross-site scripting (XSS) attacks.
     * These protections are largely unnecessary in modern browsers when sites implement a strong Content-Security-Policy
     * that disables the use of inline JavaScript ('unsafe-inline').
     * <p>
     * More <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/X-XSS-Protection">Info</a>
     */
    private XXSSProtection xXssProtection = XXSSProtection.ENABLE_MODE;

    /**
     * The HTTP Content-Security-Policy response header allows website administrators
     * to control resources the user agent is allowed to load for a given page.
     * With a few exceptions, policies mostly involve specifying server origins and script endpoints.
     * This helps guard against cross-site scripting attacks (Cross-site_scripting).
     * <p>
     * More <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Content-Security-Policy">Info</a>
     */
    private String contentSecurityPolicy;

    /**
     * The HTTP Strict-Transport-Security response header (often abbreviated as HSTS)
     * informs browsers that the site should only be accessed using HTTPS,
     * and that any future attempts to access it using HTTP should automatically be converted to HTTPS.
     * <p>
     * More <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/Strict-Transport-Security">Info</a>
     */
    private HSTS hsts;

}
