package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.stream.Collectors;

public class DataFieldsResource extends CollectionModel<Field> {

    public DataFieldsResource(Collection<Field> content, Locale locale) {
        super(new ArrayList<>(content), new ArrayList<>(), null);
    }
}