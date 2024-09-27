package com.netgrif.application.engine.petrinet.domain.dataset.logic.validation

import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

@Data
class Argument {

    private String name
    private String value
    private String dynamicValue
    private boolean dynamic

    static ArgumentBuilder builder() {
        return new ArgumentBuilder()
    }

    Argument() {
    }

    Argument(String name, String value, boolean dynamic) {
        this.name = name
        this.value = value
        this.dynamic = dynamic
    }

    Argument(String name, String value, String dynamicValue, boolean dynamic) {
        this.name = name
        this.value = value
        this.dynamicValue = dynamicValue
        this.dynamic = dynamic
    }

    Argument(String name, boolean dynamic) {
        this.name = name
        this.dynamic = dynamic
    }

    private Argument(ArgumentBuilder builder) {
        this.name = builder.getName()
        this.value = builder.getValue()
        this.dynamicValue = builder.getDynamicValue()
        this.dynamic = builder.getDynamic()
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

    String getDynamicValue() {
        return dynamicValue
    }

    void setDynamicValue(String dynamicValue) {
        this.dynamicValue = dynamicValue
    }

    static class ArgumentBuilder {

        private String name
        private String value
        private String dynamicValue
        private boolean dynamic

        ArgumentBuilder() {
        }

        ArgumentBuilder name(String name) {
            this.name = name
            return this
        }

        ArgumentBuilder dynamic(boolean dynamic) {
            this.dynamic = dynamic
            return this
        }

        ArgumentBuilder value(String value) {
            if (dynamic) {
                this.dynamicValue = value
            } else {
                this.value = value
            }
            return this
        }

        Argument build() {
            return new Argument(this)
        }

        String getName() {
            return name
        }

        String getValue() {
            return value
        }

        String getDynamicValue() {
            return dynamicValue
        }

        boolean getDynamic() {
            return dynamic
        }
    }

}
