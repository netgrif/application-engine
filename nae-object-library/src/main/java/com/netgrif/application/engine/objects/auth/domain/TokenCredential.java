package com.netgrif.application.engine.objects.auth.domain;

/**
 * A specialized implementation of {@link Credential} that handles token-based authentication.
 * This class manages authentication tokens as String values and provides default token handling functionality.
 * TokenCredential is specifically designed for token-based authentication mechanisms within the system.
 *
 * @see Credential
 */
public class TokenCredential extends Credential<String> {

    /**
     * Constructs a new TokenCredential with default values.
     * Sets the credential type to "token" and initializes other fields with default values.
     */
    public TokenCredential() {
        super();
        this.setType("token");
    }

    /**
     * Constructs a new TokenCredential with specified parameters.
     *
     * @param value   The token string value
     * @param order   The priority order of this credential
     * @param enabled Whether this token credential is enabled
     * @throws IllegalArgumentException if type is null/empty or value is null, as per parent class validation
     */
    public TokenCredential(String value, int order, boolean enabled) {
        super("token", value, order, enabled);
    }
}