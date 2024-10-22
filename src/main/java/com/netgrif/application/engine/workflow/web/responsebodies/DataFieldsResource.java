package com.netgrif.application.engine.workflow.web.responsebodies;

import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import lombok.Getter;
import lombok.Setter;
import org.springframework.hateoas.CollectionModel;

import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Collectors;

@Setter
@Getter
public class DataFieldsResource extends CollectionModel<LocalisedField> implements Serializable, Iterable<LocalisedField> {

    private static final long serialVersionUID = 73213276016133399L;

    private Collection<LocalisedField> content;

    public DataFieldsResource(Collection<Field> content, Locale locale) {
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