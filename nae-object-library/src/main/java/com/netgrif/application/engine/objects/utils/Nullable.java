package com.netgrif.application.engine.objects.utils;

import java.io.Serial;
import java.io.Serializable;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;


/**
 * A utility class representing a nullable container for a value of type {@code T}. It provides methods to handle
 * optionality in a fluent and functional programming style. This class is inspired by {@link Optional}
 * but differs in implementation and naming conventions.
 *
 * @param <T> The type of the value contained in this {@code Nullable}.
 */
public final class Nullable<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 8683452581122892189L;
    
    private static final Nullable<?> EMPTY = new Nullable<>(null);

    private final T value;

    private Nullable(T value) {
        this.value = value;
    }

    /**
     * Returns the value held by this instance.
     *
     * @return the value contained in this instance, or {@code null} if the instance is empty
     */
    public T get() {
        return value;
    }

    /**
     * Creates a new {@code Nullable} instance containing the given value.
     *
     * @param value the value to wrap in a {@code Nullable} instance, can be {@code null}
     * @return a {@code Nullable} instance wrapping the provided value
     */
    public static <T> Nullable<T> of(T value) {
        return new Nullable<>(value);
    }

    /**
     * Returns an empty {@code Nullable} instance holding no value.
     *
     * @param <T> the type of the value that can be held by this {@code Nullable} instance
     * @return an empty {@code Nullable} instance
     */
    public static <T> Nullable<T> empty() {
        @SuppressWarnings("unchecked")
        Nullable<T> EMPTY = (Nullable<T>) Nullable.EMPTY;
        return EMPTY;
    }

    /**
     * Checks if a value is present in this instance.
     *
     * @return {@code true} if the value is not {@code null}, otherwise {@code false}
     */
    public boolean isPresent() {
        return value != null;
    }

    /**
     * Checks if this instance holds no value.
     *
     * @return {@code true} if the value is {@code null}, otherwise {@code false}
     */
    public boolean isEmpty() {
        return value == null;
    }

    /**
     * If a value is present in this instance, performs the given action with the value.
     *
     * @param action the action to be performed if a value is present; must be non-null
     */
    public void ifPresent(Consumer<? super T> action) {
        if (value != null) {
            action.accept(value);
        }
    }

    /**
     * Performs the given action with the value if it is present, otherwise executes the provided empty action.
     *
     * @param action the {@code Consumer} to be executed if the value is present; must not be null
     * @param emptyAction the {@code Runnable} to be executed if the value is not present; must not be null
     */
    public void ifPresentOrElse(Consumer<? super T> action, Runnable emptyAction) {
        if (value != null) {
            action.accept(value);
        } else {
            emptyAction.run();
        }
    }

    /**
     * Converts the value held by this instance into an {@code Optional}.
     *
     * @return an {@code Optional} containing the value if it is present, or an empty {@code Optional} if the value is {@code null}
     */
    public Optional<T> toOptional() {
        return Optional.ofNullable(value);
    }

    /**
     * Filters the value contained in this {@code Nullable} instance based on the provided predicate.
     * If the value is present and satisfies the predicate, this instance is returned.
     * If the value does not satisfy the predicate or if the instance is empty, an empty {@code Nullable} is returned.
     *
     * @param predicate the predicate used to evaluate the contained value; must be non-null
     * @return this {@code Nullable} instance if the value satisfies the predicate,
     *         or an empty {@code Nullable} instance otherwise
     */
    public Nullable<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isEmpty()) {
            return this;
        } else {
            return predicate.test(value) ? this : empty();
        }
    }

    /**
     * Transforms the value contained in this {@code Nullable} instance using the provided mapping function.
     * If this {@code Nullable} instance is empty, an empty {@code Nullable} instance is returned.
     *
     * @param <U> the type of the value produced by the mapping function
     * @param mapper the function to apply to the value; must not be null
     * @return a {@code Nullable} instance containing the value produced by applying the mapping function,
     *         or an empty {@code Nullable} instance if this instance is empty
     */
    public <U> Nullable<U> map(Function<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty()) {
            return empty();
        } else {
            return Nullable.of(mapper.apply(value));
        }
    }

    /**
     * Applies the provided mapping function to the value contained in this {@code Nullable} instance,
     * and returns the {@code Nullable} instance produced by the mapping function. If this instance is empty,
     * an empty {@code Nullable} is returned.
     *
     * @param <U> the type of the value contained in the resulting {@code Nullable} instance
     * @param mapper the mapping function to apply to the value if it is present; must not be null
     * @return a {@code Nullable} instance produced by applying the mapping function to the value,
     * or an empty {@code Nullable} if this instance is empty
     */
    public <U> Nullable<U> flatMap(Function<? super T, ? extends Nullable<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty()) {
            return empty();
        } else {
            @SuppressWarnings("unchecked")
            Nullable<U> r = (Nullable<U>) mapper.apply(value);
            return Objects.requireNonNull(r);
        }
    }

    /**
     * Returns this {@code Nullable} instance if a value is present, otherwise returns the result
     * of invoking the provided {@code Supplier}.
     *
     * @param supplier the {@code Supplier} providing an alternative {@code Nullable} instance
     *                 if this instance is empty; must not be {@code null}
     * @return this {@code Nullable} instance if it contains a value, or the {@code Nullable} instance
     *         provided by the supplier if this instance is empty
     * @throws NullPointerException if the supplier is {@code null} or the {@code Nullable} instance
     *                              provided by the supplier is {@code null}
     */
    public Nullable<T> or(Supplier<? extends Nullable<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        if (isPresent()) {
            return this;
        } else {
            @SuppressWarnings("unchecked")
            Nullable<T> r = (Nullable<T>) supplier.get();
            return Objects.requireNonNull(r);
        }
    }

    /**
     * Creates a sequential {@code Stream} containing the value held by this {@code Nullable} instance,
     * if a value is present. If this instance is empty, returns an empty {@code Stream}.
     *
     * @return a {@code Stream} containing the value if it is present, or an empty {@code Stream} otherwise
     */
    public Stream<T> stream() {
        if (isEmpty()) {
            return Stream.empty();
        } else {
            return Stream.of(value);
        }
    }

    /**
     * Returns the value held by this instance if it is non-null; otherwise, returns the specified default value.
     *
     * @param other the value to be returned if the current value is null
     * @return the value held by this instance if it is non-null, or the specified default value if the value is null
     */
    public T orElse(T other) {
        return value != null ? value : other;
    }

    /**
     * Returns the value held by this instance if it is present; otherwise,
     * returns the result produced by the provided {@code Supplier}.
     *
     * @param supplier a {@code Supplier} to provide an alternative value
     *                 if the current instance holds no value; must not be {@code null}
     * @return the value contained in this instance if present, or the value
     *         obtained from the supplied {@code Supplier} if the instance is empty
     * @throws NullPointerException if the provided supplier is {@code null}
     */
    public T orElseGet(Supplier<? extends T> supplier) {
        return value != null ? value : supplier.get();
    }

    /**
     * Returns the value held by this instance if it is present; otherwise,
     * throws a {@link NoSuchElementException}.
     *
     * @return the value contained in this instance
     * @throws NoSuchElementException if no value is present in this instance
     */
    public T orElseThrow() {
        if (value == null) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    /**
     * Returns the value held by this instance if it is present, otherwise throws an exception
     * supplied by the provided {@code Supplier}.
     *
     * @param <X> the type of the exception to be thrown
     * @param exceptionSupplier the supplier that provides the exception to be thrown if no value is present; must not be null
     * @return the value held by this instance if it is present
     * @throws X if no value is present
     * @throws NullPointerException if the {@code exceptionSupplier} is null
     */
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (value != null) {
            return value;
        } else {
            throw exceptionSupplier.get();
        }
    }

    /**
     * Compares this {@code Nullable} instance with the specified object for equality.
     * Two {@code Nullable} instances are considered equal if they both contain the
     * same value or are both empty.
     *
     * @param obj the object to be compared for equality with this {@code Nullable} instance
     * @return {@code true} if the specified object is equal to this {@code Nullable} instance,
     *         {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        return obj instanceof Nullable<?> other
                && Objects.equals(value, other.value);
    }

    /**
     * Computes the hash code for this instance based on its value field.
     *
     * @return the hash code as an integer for this instance,
     * calculated using the value field or default if the field is null.
     */
    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    /**
     * Returns a string representation of the object.
     * If the encapsulated value is non-null, the string will include the value.
     * If the encapsulated value is null, a representation indicating emptiness will be returned.
     *
     * @return a string representation of the object, indicating the encapsulated value or an empty state
     */
    @Override
    public String toString() {
        return value != null
                ? ("Nullable[" + value + "]")
                : "Nullable.empty";
    }

}
