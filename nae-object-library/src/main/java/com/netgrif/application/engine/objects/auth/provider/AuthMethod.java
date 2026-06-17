package com.netgrif.application.engine.objects.auth.provider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.bson.types.ObjectId;

import java.security.Principal;
import java.util.Map;

/**
 * Interface representing an authentication method.
 */
public interface AuthMethod<T> {

    ObjectId id();

    boolean isEnabled();

    /**
     * Method to authenticate using this authentication method.
     *
     * @param authentication the authentication request object
     */
    @JsonIgnore
    Principal authenticate(Principal authentication);

    /**
     * Retrieves the name of this authentication method.
     *
     * @return the name of the authentication method
     */
    String getName();

    /**
     * Retrieves the type of this authentication method (LDAP, OpenID, etc.).
     *
     * @return the type of the authentication method
     */
    String getType();

    /**
     * Retrieves parameters associated with this authentication method, e.g., configuration.
     *
     * @return a map of parameters for the authentication method
     */
    Map<String, Object> getParameters();

    /**
     * Retrieves the configuration for this authentication method.
     *
     * @return the configuration object
     */
    AuthMethodConfig<?> getConfiguration();

    /**
     * Sets the configuration for this authentication method.
     *
     * @param configuration the configuration object
     */
    void setConfiguration(AuthMethodConfig<?> configuration);
}
