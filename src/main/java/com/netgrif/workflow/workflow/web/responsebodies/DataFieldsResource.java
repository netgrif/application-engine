package com.netgrif.workflow.workflow.web.responsebodies;

import com.netgrif.workflow.petrinet.domain.dataset.Field;
import org.springframework.hateoas.Resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataFieldsResource extends Resources<LocalisedField> {

    public DataFieldsResource(Collection<Field> content, Locale locale) {
        super(content.stream()
                .map(f -> LocalisedFieldFactory.from(f, locale))
                .collect(Collectors.toList()), new ArrayList<>());
    }
}