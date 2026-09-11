package com.netgrif.application.engine.pfql.service.formatters;

import com.netgrif.application.engine.petrinet.domain.version.Version;

// todo 2483 doc
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
