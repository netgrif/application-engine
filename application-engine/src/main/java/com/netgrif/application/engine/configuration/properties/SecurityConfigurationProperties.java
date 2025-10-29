package com.netgrif.application.engine.configuration.properties;

import com.netgrif.application.engine.configuration.properties.enumeration.XFrameOptionsMode;
import com.netgrif.application.engine.configuration.properties.enumeration.XXSSProtection;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Configuration properties for security settings of the Netgrif Engine application.
 * Properties under this class are prefixed with "netgrif.engine.security".
 */
@Data
@ConfigurationProperties(prefix = "netgrif.engine.security")
public class SecurityConfigurationProperties {

    /**
     * Defines whether Cross-Site Request Forgery (CSRF) is enabled.
     * CSRF helps prevent unauthorized commands from being transmitted from a user that the web application trusts.
     */
    private boolean csrf = false;

    /**
     * Enables or disables Cross-Origin Resource Sharing (CORS).
     * CORS allows or restricts access to resources from external domains.
     */
    private boolean cors = true;

    /**
     * A list of allowed origins for CORS.
     * Configured origins specify which domains are permitted to access resources.
     * Example: "http://localhost:4200"
     */
    private List<String> allowedOrigins;

    /**
     * Specifies the name of the authentication realm.
     * Defaults to "NAE-Realm".
     */
    private String realmName = "NAE-Realm";

    /**
     * List of authentication providers used for validating user credentials.
     */
    private String[] providers;
    

    /**
     * Defines the server-specific URL patterns that must be handled without authentication.
     * These patterns specify which endpoints should be accessible publicly.
     */
    private String[] serverPatterns;

    /**
     * Configures patterns for static resources that do not require authentication.
     * Examples include frequently accessed resources like icons, configuration files, and Swagger documentation.
     */
    private String[] staticPatterns = {"/favicon.ico", "/favicon.ico", "/manifest.json", "/manifest.json", "/configuration/**", "/swagger-resources/**", "/swagger-ui.html", "/webjars/**"};

    /**
     * Specifies URL patterns that are exceptions, where anonymous user access is permitted.
     * These patterns are excluded from typical security filters and authentication requirements.
     */
    private String[] anonymousExceptions;


    private String anonymousAuthenticationKey = "anonymousUser";

    /**
     * Headers settings
     */
    private HeadersProperties headers = new HeadersProperties();
    

    /**
     * Encryption-specific configuration properties.
     * These define encryption settings such as algorithms in use and password configuration.
     */
    private EncryptionProperties encryption = new EncryptionProperties();

    /**
     * Authentication-related configuration properties.
     * These define settings for user registration, password restrictions, and more.
     */
    private AuthProperties auth = new AuthProperties();

    /**
     * Static security configuration properties.
     * These settings determine whether static security features should be enabled.
     */
    private StaticProperties staticSecurity = new StaticProperties();

    /**
     * Configuration for security-related limits.
     * Includes settings like maximum login attempts and blocking behavior.
     */
    private SecurityLimitsProperties limits = new SecurityLimitsProperties();

    /**
     * JWT-specific configuration properties.
     * Defines settings related to token expiration, algorithms, and private key locations.
     */
    private JwtProperties jwt = new JwtProperties();

    /**
     * Web settings configuration properties.
     * This includes enabling or disabling specific web components or modules such as case, user, or task functionalities.
     */
    private WebProperties web = new WebProperties();

    /**
     * Configuration properties for encryption settings.
     * Properties under this class are prefixed with "netgrif.engine.security.encryption".
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.encryption")
    public static class EncryptionProperties {
        /**
         * Password used for encryption operations.
         */
        private String password;

        /**
         * Algorithm used for encryption, such as "AES" or "RSA".
         */
        private String algorithm;
    }


    /**
     * Authentication-related configuration properties.
     * Properties under this class are prefixed with "netgrif.engine.security.auth".
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.auth")
    public static class AuthProperties {

        /**
         * Enables or disables an open registration process.
         * Default value is true.
         */
        private boolean openRegistration = true;

        /**
         * Validity period for authentication tokens in days.
         * Default value is 3 days.
         */
        private int tokenValidityPeriod = 3;

        /**
         * Specifies the minimum required length for user passwords.
         * Default value is 8 characters.
         */
        private int minimalPasswordLength = 8;

        /**
         * Enables or disables profile editing functionality.
         * Default value is true.
         */
        private boolean enableProfileEdit = true;

        /**
         * Defines URL patterns that do not require authentication.
         * Default is an empty array.
         */
        private String[] noAuthenticationPatterns = new String[0];

        /**
         * Specifies the administrator's password.
         */
        private String adminPassword;

        /**
         * Determines if a super administrator account should be created during setup.
         * Default value is true.
         */
        private boolean createSuper = true;
    }

    /**
     * Static security configuration properties.
     * Properties under this class are prefixed with "netgrif.engine.security.static".
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.static")
    public static class StaticProperties {

        /**
         * Enables or disables static security features.
         * Default value is false.
         */
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

        /**
         * Configuration for HTTP Strict-Transport-Security (HSTS) settings.
         * Properties under this class are prefixed with "netgrif.engine.security.headers.hsts".
         */
        @Data
        @ConfigurationProperties(prefix = "netgrif.engine.security.headers.hsts")
        public static class HSTS {

            /**
             * Enables or disables HSTS.
             * If enabled, the browser enforces HTTPS connections.
             * Default is true.
             */
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

    /**
     * Security limits configuration properties.
     * Properties under this class are prefixed with "netgrif.engine.security.limits".
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.limits")
    public static class SecurityLimitsProperties {

        /**
         * Maximum allowable login attempts before blocking the account.
         * Default value is 10.
         */
        private int loginAttempts = 10;

        /**
         * Duration for login blocking after exceeding maximum login attempts.
         * Default is 10 units of time as defined in loginTimeoutUnit.
         */
        private int loginTimeout = 10;

        /**
         * Time unit for login timeout duration.
         * Default is MINUTES.
         */
        private TimeUnit loginTimeoutUnit = TimeUnit.MINUTES;

        /**
         * Defines maximum email send attempts before blocking the account.
         * Default value is 2.
         */
        private int emailSendsAttempts = 2;

        /**
         * Duration for blocking email after exceeding the limit of send attempts.
         * Default is 1.
         */
        private int emailBlockDuration = 1;

        /**
         * Time unit for email block duration.
         * Default is DAYS.
         */
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

    /**
     * Web-related configuration properties.
     * Properties under this class are prefixed with "netgrif.engine.security.web".
     */
    @Data
    @ConfigurationProperties(prefix = "netgrif.engine.security.web")
    public static class WebProperties {

        /**
         * Enables user-related web functionalities.
         * Default value is true.
         */
        public boolean userEnabled = true;

        /**
         * Enables Petri net-related web functionalities.
         * Default value is true.
         */
        public boolean petriNetEnabled = true;

        /**
         * Enables case-related web functionalities.
         * Default value is true.
         */
        public boolean caseEnabled = true;

        /**
         * Enables task-related web functionalities.
         * Default value is true.
         */
        public boolean taskEnabled = true;

        /**
         * Enables authentication-related web functionalities.
         * Default value is true.
         */
        public boolean authEnabled = true;

        /**
         * Enables Elasticsearch-related web functionalities.
         * Default value is true.
         */
        public boolean elasticEnabled = true;

        /**
         * Enables impersonation-related web functionalities.
         * Default value is true.
         */
        public boolean impersonationEnabled = true;

        /**
         * Enables session-related web functionalities.
         * Default value is true.
         */
        public boolean sessionEnabled = true;

        /**
         * Enables group-related web functionalities.
         * Default value is true.
         */
        public boolean groupEnabled = true;

        /**
         * Configuration for publicly accessible web settings.
         */
        public PublicProperties publicWeb = new PublicProperties();

        /**
         * Configuration properties for publicly available web functionalities.
         */
        @Data
        public static class PublicProperties {

            private boolean enabled = true;

            /**
             * Public URL for web functionalities.
             */
            private String url;

            /**
             * Enables Petri net-related public web functionalities.
             * Default value is true.
             */
            public boolean petriNetEnabled = true;

            /**
             * Enables case-related public web functionalities.
             * Default value is true.
             */
            public boolean caseEnabled = true;

            /**
             * Enables task-related public web functionalities.
             * Default value is true.
             */
            public boolean taskEnabled = true;

            /**
             * Enables user-related public web functionalities.
             * Default value is true.
             */
            public boolean userEnabled = true;
        }
    }
}
