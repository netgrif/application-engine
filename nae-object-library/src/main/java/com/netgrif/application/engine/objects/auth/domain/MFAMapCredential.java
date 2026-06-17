package com.netgrif.application.engine.objects.auth.domain;

import java.util.LinkedHashMap;

/**
 * Represents a Multi-Factor Authentication (MFA) credential that stores data in a map structure.
 * This class extends the generic {@link Credential} class specifically for {@link LinkedHashMap} type values
 * where keys are strings and values can be any object. Used to store complex MFA-related credentials
 * that require multiple key-value pairs to be stored in an ordered manner.
 *
 * @see Credential
 */
public class MFAMapCredential extends Credential<LinkedHashMap<String, Object>> {

    /**
     * Constructs a new MFA map credential with default values.
     * Sets the credential type to "MFA".
     */
    public MFAMapCredential() {
        super();
        this.setType("MFA");
    }

    /**
     * Constructs a new MFA map credential with specified parameters.
     * The actual credential type will be prefixed with "MFA-".
     *
     * @param type the specific type of MFA credential (will be prefixed with "MFA-")
     * @param value the map containing the credential data
     * @param order the priority order of this credential
     * @param enabled whether this credential is enabled
     * @throws IllegalArgumentException if type is null/empty or value is null, as per parent class validation
     */
    public MFAMapCredential(String type, LinkedHashMap<String, Object> value, int order, boolean enabled) {
        super("MFA-" + type, value, order, enabled);
    }
}