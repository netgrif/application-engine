package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.workflow.domain.dataset.Field;
import org.springframework.hateoas.CollectionModel;

import java.util.ArrayList;
import java.util.List;

public class DataFieldsResource extends CollectionModel<Field<?>> {

    public DataFieldsResource(List<Field<?>> content) {
        super(content, new ArrayList<>(), null);
    }
}