package com.netgrif.application.engine.authentication.domain.constants;

public class AnonymIdentityConstants {
    public static final String USERNAME_TEMPLATE = "%s@netgrif.com";
    public static final String FIRSTNAME = "Anonymous";
    public static final String LASTNAME = "Identity";

    /**
     * todo javadoc
     */
    public static String usernameOf(String usernamePrefix) {
        return String.format(USERNAME_TEMPLATE, usernamePrefix);
    }
}
