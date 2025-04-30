package com.netgrif.application.engine.objects.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Attribute<T> implements Serializable {


    private T value;
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

    public void setValue(T value) {
        if (required && value == null) {
            throw new IllegalArgumentException("Required attribute cannot be null.");
        }
        this.value = value;
        this.lastUpdated = LocalDateTime.now();
    }
}
