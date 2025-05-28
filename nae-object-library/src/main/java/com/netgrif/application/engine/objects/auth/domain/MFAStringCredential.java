package com.netgrif.application.engine.objects.auth.domain;

/**
 * Represents a Multi-Factor Authentication (MFA) credential that stores string values.
 * This class extends the generic {@link Credential} class specifically for String type values.
 * Used to store various MFA-related credentials such as TOTP secrets, backup codes, or other
 * string-based authentication factors.
 *
 * @see Credential
 */
public class MFAStringCredential extends Credential<String> {

    /**
     * Constructs a new MFA string credential with default values.
     * Sets the credential type to "MFA".
     */
    public MFAStringCredential() {
        super();
        this.setType("MFA");
    }

    /**
     * Constructs a new MFA string credential with specified parameters.
     * The actual credential type will be prefixed with "MFA-".
     *
     * @param type the specific type of MFA credential (will be prefixed with "MFA-")
     * @param value the string value of the credential
     * @param order the priority order of this credential
     * @param enabled whether this credential is enabled
     * @throws IllegalArgumentException if type is null/empty or value is null, as per parent class validation
     */
    public MFAStringCredential(String type, String value, int order, boolean enabled) {
        super("MFA-" + type, value, order, enabled);
    }
}