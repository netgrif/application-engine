package com.netgrif.application.engine.importer.service.builder;

import com.netgrif.application.engine.importer.model.Data;
import com.netgrif.application.engine.importer.model.Option;
import com.netgrif.application.engine.importer.model.Options;
import com.netgrif.application.engine.workflow.domain.dataset.FieldWithAllowedRoles;

import java.util.Set;
import java.util.stream.Collectors;

public abstract class FieldWithAllowedRolesBuilder<T extends FieldWithAllowedRoles<U>, U> extends FieldBuilder<T> {

    public void setRoles(T field, Data data) {
        Options options = data.getOptions();
        if (options == null || options.getOption() == null || options.getOption().isEmpty()) {
            return;
        }
        Set<String> roles = options.getOption().stream()
                .map(Option::getValue).collect(Collectors.toSet());
        field.setRoles(roles);
    }
}
