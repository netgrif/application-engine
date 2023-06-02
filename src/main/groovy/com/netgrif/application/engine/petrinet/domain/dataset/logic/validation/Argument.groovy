package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
class Argument {

    protected String name
    protected String value
    protected boolean dynamic

    Argument(String name, String value, boolean dynamic) {
        this.name = name
        this.value = value
        this.dynamic = dynamic
    }

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getValue() {
        return value
    }

    void setValue(String value) {
        this.value = value
    }

    boolean getDynamic() {
        return dynamic
    }

    void setDynamic(boolean dynamic) {
        this.dynamic = dynamic
    }
}
