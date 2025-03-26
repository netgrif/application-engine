package com.netgrif.application.engine.authentication.domain;

import com.netgrif.application.engine.configuration.security.factory.AdvancedPublicAuthenticationFilterFactory;
import com.netgrif.application.engine.configuration.security.factory.BasicPublicAuthenticationFilterFactory;
import com.netgrif.application.engine.configuration.security.factory.SimplePublicAuthenticationFilterFactory;

/**
 * todo javadoc
 * identity vs actor
 * warning for changing strategy in running system
 */
public enum PublicStrategy {
    /** todo javadoc */
    SIMPLE(SimplePublicAuthenticationFilterFactory.class),

    /** todo javadoc */
    BASIC(BasicPublicAuthenticationFilterFactory.class),

    /** todo javadoc */
    ADVANCED(AdvancedPublicAuthenticationFilterFactory.class);

    public final Class<?> factoryClass;

    PublicStrategy(Class<?> factoryClass) {
        this.factoryClass = factoryClass;
    }
}
