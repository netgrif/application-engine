package com.netgrif.application.engine.configuration.properties;

import com.netgrif.application.engine.configuration.properties.enumeration.XFrameOptionsMode;
import com.netgrif.application.engine.configuration.properties.enumeration.XXSSProtection;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Data
@ConfigurationProperties(prefix = "netgrif.engine.security")
public class SecurityConfigurationProperties {

    /**
     * Defines whether Cross Site Request Forgery is enabled
     */
    private boolean csrf = true;

    /**
     * Enable CORS (Cross-Origin Resource)
     */
    private boolean cors = true;

    /**
     * List of Origins for CORS
     * Example: nae.server.security.allowed-origins=http://localhost:4200
     */
    private List<String> allowedOrigins;

    private String realmName;

    private String[] providers;

    private String[] serverPatterns;

    private String[] staticPatterns = {"/favicon.ico", "/favicon.ico", "/manifest.json", "/manifest.json", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"};

    private String[] anonymousExceptions;

    /**
     * Headers settings
     */
    private HeadersProperties headers = new HeadersProperties();

    private EncryptionProperties encryption = new EncryptionProperties();

    private AuthProperties auth = new AuthProperties();

    private StaticProperties staticSecurity = new StaticProperties();

    private SecurityLimitsProperties limits = new SecurityLimitsProperties();

    private JwtProperties jwt = new JwtProperties();

    private WebProperties web = new WebProperties();

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.encryption")
    public static class EncryptionProperties {
        private String password;
        private String algorithm;
    }

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.auth")
    public static class AuthProperties {

        private boolean openRegistration = true;

        private int tokenValidityPeriod = 3;

        private int minimalPasswordLength = 8;

        private boolean enableProfileEdit = true;

        private String[] noAuthenticationPatterns = new String[0];

        private String adminPassword;

        private boolean createSuper = true;
    }

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.static")
    public static class StaticProperties {
        private boolean enabled = false;
    }

    @Data
    @Configuration
    @ConfigurationProperties(prefix = "netgrif.engine.security.headers")
    public static class HeadersProperties {

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
        private HSTS hsts = new HSTS();

        @Data
        @ConfigurationProperties(prefix = "netgrif.engine.security.headers.hsts")
        public static class HSTS {

            private boolean enable = true;

            /**
             * The time, in seconds, that the browser should remember that a site is only to be accessed using HTTPS.
             * <p>
             * Default value: 31536000
             */
            private long maxAge = 31536000;

            /**
             * If this optional parameter is specified, this rule applies to all of the site's subdomains as well.
             */
            private boolean includeSubDomains = true;

            /**
             * See Preloading Strict Transport Security for details.
             * When using preload, the max-age directive must be at least 31536000 (1 year),
             * and the includeSubDomains directive must be present. Not part of the specification.
             * <p>
             */
            private boolean preload;
        }
    }

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.limits")
    public static class SecurityLimitsProperties {

        private int loginAttempts = 10;

        private int loginTimeout = 10;

        private TimeUnit loginTimeoutUnit = TimeUnit.MINUTES;

        private int emailSendsAttempts = 2;

        private int emailBlockDuration = 1;

        private TimeUnit emailBlockTimeType = TimeUnit.DAYS;
    }

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.jwt")
    public static class JwtProperties {

        /**
         * Defines the validity duration for a token in milliseconds, then expiration dateTime is
         * counted using "System.currentTimeMillis() + this.expiration"
         */
        private long expiration = 900000;

        /**
         * Defines path to a file that contains generated private key with certificate
         */
        private Resource privateKey;

        /**
         * Defines which algorithm is used when generating JWT token
         */
        private String algorithm = "RSA";
    }

    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.web")
    public static class WebProperties {
        public boolean userEnabled = true;
        public boolean petriNetEnabled = true;
        public boolean caseEnabled = true;
        public boolean taskEnabled = true;
        public boolean authEnabled = true;
        public boolean elasticEnabled = true;
        public boolean impersonationEnabled = true;
        public boolean sessionEnabled = true;
        public boolean groupEnabled = true;
        public PublicProperties publicWeb = new PublicProperties();

        @Data
        public static class PublicProperties {
            private String url;
            public boolean petriNetEnabled = true;
            public boolean caseEnabled = true;
            public boolean taskEnabled = true;
            public boolean userEnabled = true;
        }
    }
}
