package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.core.petrinet.domain.dataset.Field;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.CollectionModel;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Collectors;

@Setter
@Getter
public class DataFieldsResource extends CollectionModel<LocalisedField> implements com.netgrif.core.workflow.domain.DataFieldsResource<LocalisedField>, Serializable, Iterable<LocalisedField> {

    @Serial
    private static final long serialVersionUID = 73213276016133399L;

    private Collection<LocalisedField> content;

    public DataFieldsResource(Collection<Field<?>> content, Locale locale) {
        super();
        this.content = content.stream()
                .map(f -> LocalisedFieldFactory.from(f, locale))
                .collect(Collectors.toList());
    }

    @Override
    public Iterator<LocalisedField> iterator() {
        return this.content.iterator();
    }
}