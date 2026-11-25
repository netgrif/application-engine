package com.netgrif.application.engine.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration properties for authority management in the Netgrif Application Engine.
 * <p>
 * This class provides configuration for default authorities assigned to different user types
 * and allows defining additional custom authorizing objects.
 * <p>
 * Configuration properties are bound with the prefix {@code netgrif.engine.authority}.
 * <p>
 * Example configuration in application.properties or application.yml:
 * <pre>
 * netgrif.engine.authority.default-user-authorities=USER_READ,USER_WRITE
 * netgrif.engine.authority.default-anonymous-authorities=PUBLIC_READ
 * netgrif.engine.authority.default-admin-authorities=ADMIN_ALL
 * netgrif.engine.authority.additional-authorizing-objects=CUSTOM_OBJECT_1,CUSTOM_OBJECT_2
 * </pre>
 *
 * @see com.netgrif.application.engine.auth.service.BaseAuthorizationServiceAspect
 * @see com.netgrif.application.engine.startup.runner.AuthorityRunner
 */
@Data
@Component
@ConfigurationProperties(prefix = "netgrif.engine.authority")
public class AuthorityConfigurationProperties {

    /**
     * List of default authorities assigned to regular users upon creation.
     * <p>
     * These authorities define the baseline permissions for standard user accounts
     * in the application.
     */
    private List<String> defaultUserAuthorities = new ArrayList<>();

    /**
     * List of default authorities assigned to anonymous (unauthenticated) users.
     * <p>
     * These authorities define the permissions available to users who are not
     * logged into the system.
     */
    private List<String> defaultAnonymousAuthorities = new ArrayList<>();

    /**
     * List of default authorities assigned to administrator users.
     * <p>
     * These authorities define elevated permissions for users with administrative
     * privileges in the application.
     */
    private List<String> defaultAdminAuthorities = new ArrayList<>();

    /**
     * List of additional custom authorizing objects to be created at application startup.
     * <p>
     * This allows extending the authorization system with custom authority types beyond
     * the predefined ones. These objects will be created by the {@code AuthorityRunner}
     * during application initialization.
     *
     * @see com.netgrif.application.engine.startup.runner.AuthorityRunner
     */
    private List<String> additionalAuthorizingObjects = new ArrayList<>();
}
