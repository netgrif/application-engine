package com.netgrif.workflow.petrinet.domain.dataset

import lombok.Getter
import org.springframework.data.mongodb.core.mapping.Document

@Document
public class MultichoiceField extends Field<Set<String>> {

    @Getter
    private Set<String> choices;

    public MultichoiceField() {
        super();
        value = new HashSet<>();
        choices = new HashSet<>();
    }

    public MultichoiceField(String[] values) {
        this();
        if (values != null) {
            choices.addAll(Arrays.asList(values));
        }
    }

    public void setValue(List<String> value) {
        this.value = new HashSet<>(value);
    }
}