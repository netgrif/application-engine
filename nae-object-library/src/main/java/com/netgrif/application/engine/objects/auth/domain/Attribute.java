package com.netgrif.application.engine.objects.auth.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class Attribute<T> implements Serializable {

    private T value;

    @Setter
    private boolean required;

    private LocalDateTime lastUpdated = LocalDateTime.now();

    public Attribute(T value, boolean required) {
        this.value = value;
        this.required = required;
        this.lastUpdated = LocalDateTime.now();
    }

    public Attribute(boolean required) {
        this.value = null;
        this.required = required;
        this.lastUpdated = LocalDateTime.now();
    }

    public Attribute(T value) {
        this.value = value;
        this.required = false;
        this.lastUpdated = LocalDateTime.now();
    }

    public void setValue(T value) {
        if (required && value == null) {
            throw new IllegalArgumentException("Required attribute cannot be null.");
        }
        this.value = value;
        this.lastUpdated = LocalDateTime.now();
    }
}
