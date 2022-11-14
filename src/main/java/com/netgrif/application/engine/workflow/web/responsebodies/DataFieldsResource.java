package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
import java.util.Collection;

public class DataFieldsResource extends CollectionModel<Field<?>> {

    public DataFieldsResource(Collection<Field<?>> content) {
        super(new ArrayList<>(content), new ArrayList<>(), null);
    }
}