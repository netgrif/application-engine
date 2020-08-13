package com.netgrif.workflow.configuration;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.engine.jdbc.env.spi.JdbcEnvironment;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.orm.jpa.hibernate.SpringPhysicalNamingStrategy;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
public class HibernateNamingStrategy extends SpringPhysicalNamingStrategy {

    @Value("${hibernate.naming-strategy.ignored-values:DTYPE}")
    private List<String> ignored;

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
                ignored.contains(name));
    }

}
