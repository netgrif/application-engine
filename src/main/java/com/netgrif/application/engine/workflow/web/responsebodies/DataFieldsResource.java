package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataFieldsResource extends CollectionModel<LocalisedField> {

    public DataFieldsResource(Collection<Field> content, Locale locale) {
        super(content.stream()
                .map(f -> LocalisedFieldFactory.from(f, locale))
                .collect(Collectors.toList()), new ArrayList<>(), null);
    }
}