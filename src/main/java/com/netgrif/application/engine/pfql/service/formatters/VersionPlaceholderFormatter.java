package com.netgrif.application.engine.pfql.service.formatters;

import com.netgrif.application.engine.petrinet.domain.version.Version;

/**
 * Formatter for Version objects used in PFQL queries.
 * <p>
 * This formatter handles the conversion of {@link Version} objects into their string
 * representation for use in PFQL query placeholders. It supports Version objects and
 * formats them by calling their {@code toString()} method.
 * </p>
 */
public class VersionPlaceholderFormatter implements QueryLangPlaceholderFormatter {

    @Override
    public boolean supports(Object value) {
        return value instanceof Version;
    }

    @Override
    public String format(Object value) {
        return value.toString();
    }
}
