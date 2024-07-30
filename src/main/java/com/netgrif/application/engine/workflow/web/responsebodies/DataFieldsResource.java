package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import org.springframework.hateoas.CollectionModel;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataFieldsResource extends CollectionModel<LocalisedField> implements Serializable {

    @Serial
    private static final long serialVersionUID = 73213276016133399L;

    public DataFieldsResource(LinkedList<Field> content, Locale locale) {
        super(content.stream()
                .map(f -> LocalisedFieldFactory.from(f, locale))
                .collect(Collectors.toList()), new ArrayList<>(), null);
    }
}