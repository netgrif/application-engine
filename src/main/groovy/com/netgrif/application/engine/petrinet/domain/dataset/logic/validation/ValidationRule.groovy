package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
@NoArgsConstructor
@AllArgsConstructor
class ValidationRule {

    protected String name
    protected String rule
    protected boolean dynamic

    String getName() {
        return name
    }

    void setName(String name) {
        this.name = name
    }

    String getRule() {
        return rule
    }

    void setRule(String rule) {
        this.rule = rule
    }

    boolean getDynamic() {
        return dynamic
    }

    void setDynamic(boolean dynamic) {
        this.dynamic = dynamic
    }
}
