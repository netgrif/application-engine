package com.netgrif.workflow.petrinet.domain.dataset

import org.springframework.data.mongodb.core.mapping.Document

@Document
public class MultichoiceField extends ChoiceField<Set<String>> {

    public MultichoiceField() {
        super();
        value = new HashSet<>();
    }

    MultichoiceField(String[] values) {
        super(values)
    }

    @Override
    public void setDefaultValue(String value){
        String[] vls = value.split(",")
        vls.each {s -> s.trim()}
        this.defaultValue = new HashSet<String>(vls as Set)
    }

    @Override
    void clearValue() {
        super.clearValue()
        setValue(getDefaultValue())
    }

    public void setValue(List<String> value) {
        this.value = new HashSet<>(value);
    }
}