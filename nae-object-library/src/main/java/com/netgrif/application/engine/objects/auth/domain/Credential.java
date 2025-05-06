package com.netgrif.application.engine.objects.auth.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Credential<T> implements Comparable<Credential<T>>, Serializable {

    protected String type;

    protected T value;

    protected LocalDateTime createdAt = LocalDateTime.now();

    protected LocalDateTime lastUpdated;

    protected int order;

    protected boolean enabled;

    protected Map<String, Object> properties = new HashMap<>();

    // TODO: constructor with no value required

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

    public Credential(String type, T value, LocalDateTime createdAt, int order) {
        this.type = type;
        this.value = value;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.order = order;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * Updates the credential value and refreshes the lastUpdated timestamp.
     *
     * @param newValue The new value for the credential.
     */
    public Credential<T> updateValue(T newValue) {
        this.value = newValue;
        this.lastUpdated = LocalDateTime.now();
        return this;
    }

    /**
     * Adds or updates a property in the credential.
     */
    public void addProperty(String key, Object value) {
        this.properties.put(key, value);
    }

    /**
     * Retrieves a property from the credential by key.
     */
    public Object getProperty(String key) {
        return this.properties.get(key);
    }

    @Override
    public int compareTo(Credential<T> other) {
        int orderComparison = Integer.compare(this.order, other.order);
        if (orderComparison != 0) {
            return orderComparison;
        }
        return this.createdAt.compareTo(other.createdAt);
    }

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
