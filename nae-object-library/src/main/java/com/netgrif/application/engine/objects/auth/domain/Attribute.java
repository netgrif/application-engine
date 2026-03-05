package com.netgrif.application.engine.objects.auth.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * A generic class representing an attribute that can hold a value of any type with additional metadata.
 * This class provides functionality for storing, validating, and managing attributes with their values
 * and requirement status. It also tracks when the attribute was last modified.
 *
 * @param <T> The type of the value stored in this attribute
 */
@Getter
@NoArgsConstructor
public class Attribute<T> implements Serializable {

    /**
     * The actual value stored in this attribute.
     * Can be null if not set or explicitly set to null.
     */
    private T value;

    /**
     * Indicates whether this attribute must have a non-null value.
     * When true, attempts to set null value will throw an IllegalArgumentException.
     */
    @Setter
    private boolean required;

    /**
     * Timestamp indicating when this attribute was last modified.
     * Automatically updated whenever the value is changed.
     */
    private LocalDateTime lastUpdated = LocalDateTime.now();

    /**
     * Creates an Attribute instance with the specified value.
     * The created attribute will have the `required` flag set to false by default.
     *
     * @param <T>   The type of the value to be stored in the attribute
     * @param value The initial value of the attribute
     * @return A new Attribute instance containing the specified value
     */
    public static <T> Attribute<T> of(T value) {
        return new Attribute<>(value);
    }

    /**
     * Creates an Attribute instance with the specified value and required status.
     *
     * @param <T>   The type of the value to be stored in the attribute
     * @param value The initial value of the attribute
     * @return A new Attribute instance containing the specified value
     */
    public static <T> Attribute<T> required(T value) {
        return new Attribute<>(value, true);
    }

    /**
     * Creates a new attribute with the specified value and required status.
     *
     * @param value    The initial value of the attribute
     * @param required Whether the attribute must have a non-null value
     * @throws IllegalArgumentException if required is true and value is null
     */
    public Attribute(T value, boolean required) {
        if (required && value == null) {
            throw new IllegalArgumentException("Required attribute cannot be null.");
        }
        this.value = value;
        this.required = required;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Creates a new optional attribute with the specified value.
     * The required flag will be set to false.
     *
     * @param value The initial value of the attribute
     */
    public Attribute(T value) {
        this.value = value;
        this.required = false;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Sets a new value for this attribute and updates the lastUpdated timestamp.
     * If the attribute is required and the new value is null, throws an IllegalArgumentException.
     *
     * @param value The new value to set
     * @throws IllegalArgumentException if the attribute is required and the value is null
     */
    public void setValue(T value) {
        if (required && value == null) {
            throw new IllegalArgumentException("Required attribute cannot be null.");
        }
        this.value = value;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Checks if the attribute has a null value.
     *
     * @return true if the attribute is empty (null), false otherwise
     */
    public boolean hasNullValue() {
        return value == null;
    }
}