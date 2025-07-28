package com.netgrif.application.engine.objects.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Abstract class representing a credential in the authentication system.
 * Implements {@link Comparable} for ordering credentials and {@link Serializable} for object serialization.
 *
 * @param <T> The type of the credential value
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Credential<T> implements Comparable<Credential<T>>, Serializable {

    /**
     * The type identifier of the credential.
     */
    protected String type;

    /**
     * The actual value of the credential.
     */
    protected T value;

    /**
     * Timestamp when the credential was created.
     */
    protected LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Timestamp of the last update to the credential.
     */
    protected LocalDateTime lastUpdated;

    /**
     * Order priority of the credential.
     */
    protected int order;

    /**
     * Flag indicating whether the credential is enabled.
     */
    protected boolean enabled;

    /**
     * Additional properties associated with the credential.
     */
    protected Map<String, Object> properties = new HashMap<>();

    // TODO: constructor with no value required

    /**
     * Constructs a new credential with specified parameters.
     *
     * @param type    The type identifier of the credential
     * @param value   The value of the credential
     * @param order   The order priority
     * @param enabled Whether the credential is enabled
     * @throws IllegalArgumentException if type is null/empty or value is null
     */
    public Credential(String type, T value, int order, boolean enabled) {
        if (type == null || type.isEmpty()) {
            throw new IllegalArgumentException("Credential type cannot be null or empty");
        }
        if (value == null) {
            throw new IllegalArgumentException("Credential value cannot be null or empty");
        }

        this.type = type;
        this.value = value;
        this.order = order;
        this.enabled = enabled;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Constructs a new credential with specified parameters and creation timestamp.
     *
     * @param type      The type identifier of the credential
     * @param value     The value of the credential
     * @param createdAt The creation timestamp
     * @param order     The order priority
     */
    public Credential(String type, T value, LocalDateTime createdAt, int order) {
        this.type = type;
        this.value = value;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.order = order;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Updates the credential value and refreshes the last update timestamp.
     *
     * @param newValue The new value to set
     * @return The updated credential instance
     */
    public Credential<T> updateValue(T newValue) {
        this.value = newValue;
        this.lastUpdated = LocalDateTime.now();
        return this;
    }

    /**
     * Adds or updates a property in the credential's properties map.
     *
     * @param key   The property key
     * @param value The property value
     */
    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    /**
     * Retrieves a property value from the credential's properties map.
     *
     * @param key The property key
     * @return The property value or null if not found
     */
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    /**
     * Compares this credential with another based on order and creation time.
     * 
     * @param other The credential to compare with
     * @return negative if this credential should be ordered before the other,
     *         positive if after, and zero if they are equal in ordering
     */
    @Override
    public int compareTo(Credential<T> other) {
        int orderComparison = Integer.compare(this.order, other.order);
        if (orderComparison != 0) {
            return orderComparison;
        }
        return this.createdAt.compareTo(other.createdAt);
    }

    /**
     * Returns a string representation of the credential with protected value.
     *
     * @return A string representation of the credential
     */
    @Override
    public String toString() {
        String displayValue = "PROTECTED";
        return "Credential{" +
                "type='" + type + '\'' +
                ", value='" + displayValue + '\'' +
                ", createdAt=" + createdAt +
                ", lastUpdated=" + lastUpdated +
                ", order=" + order +
                ", enabled=" + enabled +
                ", properties=" + properties +
                '}';
    }
}