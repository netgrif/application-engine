package com.netgrif.application.engine.objects.auth.domain;

/**
 * Represents a password-based credential in the authentication system.
 * Extends the generic {@link Credential} class specifically for String-type password values.
 * This class handles password storage and validation while maintaining the credential properties
 * defined by its parent class.
 * 
 * @see Credential
 */
public class PasswordCredential extends Credential<String> {

    /**
     * Constructs a new empty password credential with type set to "password".
     * The credential is initialized without a value but with the appropriate type identifier.
     */
    public PasswordCredential() {
        super();
        this.setType("password");
    }

    /**
     * Constructs a new password credential with the specified parameters.
     *
     * @param value   The password value to store
     * @param order   The priority order of this credential
     * @param enabled Flag indicating whether this credential is enabled
     * @throws IllegalArgumentException if type is null/empty or value is null, as per parent class validation
     */
    public PasswordCredential(String value, int order, boolean enabled) {
        super("password", value, order, enabled);
    }
}