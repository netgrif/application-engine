package com.netgrif.workflow.configuration;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;

import java.util.Locale;

public class HibernateNamingStrategy extends SpringPhysicalNamingStrategy {

    public static final String HIBERNATE_SPECIAL_COLUMN = "DTYPE";

    @Override
    protected Identifier getIdentifier(String name, boolean quoted, JdbcEnvironment jdbcEnvironment) {
        if (shouldSetLowercase(name, jdbcEnvironment)) {
            name = name.toLowerCase(Locale.ROOT);
        }

        return new Identifier(name, quoted);
    }

    protected boolean shouldSetLowercase(String name, JdbcEnvironment jdbcEnvironment) {
        return isCaseInsensitive(jdbcEnvironment) && (
                !StringUtils.isAllUpperCase(name.replaceAll("[\\_\\s\\.\\d]+", "")) ||
                HIBERNATE_SPECIAL_COLUMN.equals(name));
    }

}
