package com.netgrif.application.engine.objects.auth.domain;

/**
 * A specialized implementation of {@link Credential} that handles String-based credentials.
 * This class provides functionality for storing and managing credentials where the value
 * is represented as a String, such as passwords, tokens, or other text-based authentication data.
 * 
 * @see Credential
 */
public class StringCredential extends Credential<String> {

    /**
     * Constructs a new empty StringCredential.
     * Creates an instance with no initial values, using the parent class's default constructor.
     */
    public StringCredential() {
        super();
    }

    /**
     * Constructs a new StringCredential with specified parameters.
     *
     * @param type    The type identifier for this credential (e.g., "password", "api_token")
     * @param value   The String value of the credential
     * @param order   The priority order of this credential
     * @param enabled Flag indicating whether this credential is currently enabled
     * @throws IllegalArgumentException if type is null/empty or value is null, as per parent class validation
     */
    public StringCredential(String type, String value, int order, boolean enabled) {
        super(type, value, order, enabled);
    }
}