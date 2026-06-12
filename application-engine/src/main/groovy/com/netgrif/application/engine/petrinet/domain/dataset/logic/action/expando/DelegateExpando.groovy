package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.expando

import com.netgrif.application.engine.utils.ClosureSignatureMatcher

/**
 * A specialized {@link Expando} implementation that enables dynamic method invocation with overloading support for process-scoped actions.
 * <p>
 * This class is designed to attach and execute process-scoped actions to {@link com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate}
 * in a proper Groovy-like manner. It extends the standard {@link Expando} behavior by intercepting method calls
 * and executing them as closures with proper delegation settings.
 * </p>
 * <p>
 * Unlike standard {@link Expando}, this implementation supports method overloading by storing multiple closures
 * per method name in an internal function map. When a closure is set as a property, it is accumulated in a list
 * associated with that property name, allowing multiple method signatures to coexist.
 * </p>
 * <p>
 * When a method is called that doesn't exist on this object, the {@link DelegateExpando#methodMissing(String, Object)} method
 * is invoked, which looks up the method name in the function map and searches for a matching closure based on
 * parameter count and type compatibility. If a matching closure is found, it is rehydrated with this instance as
 * the delegate, owner, and thisObject, configured with {@link Closure#DELEGATE_FIRST} resolution strategy, and executed.
 * </p>
 * <p>
 * The closure rehydration process ensures that the closure executes in the context of this {@link DelegateExpando}
 * instance, allowing it to access properties and methods defined on this object while maintaining proper delegation
 * semantics for the ActionDelegate pattern.
 * </p>
 * <p>
 * <b>Usage Example:</b>
 * <pre>{@code
 * def expando = new DelegateExpando()
 * expando.greet = { String name -> "Hello, ${name}!" }
 * expando.greet = { String name, String title -> "Hello, ${title} ${name}!" }
 *
 * expando.greet("John")              // Returns "Hello, John!"
 * expando.greet("Smith", "Dr.")      // Returns "Hello, Dr. Smith!"
 *}</pre>
 * </p>
 * <p>
 * This class is primarily used by {@link FieldActionsRunner} to dynamically attach process-scoped functions
 * to action delegates, enabling flexible and context-aware execution of workflow actions.
 * </p>
 *
 * @see Expando* @see com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate* @see FieldActionsRunner
 */
class DelegateExpando extends Expando {

    private static final String DO_CALL = "doCall"

    /**
     * Internal storage for closures mapped by method name.
     * <p>
     * Each method name can map to multiple closures, enabling method overloading based on parameter types.
     * The list maintains insertion order to preserve the sequence in which overloaded methods are defined.
     * </p>
     */
    private final Map<String, List<Closure>> functionMap = new LinkedHashMap<>()

    /**
     * Returns the delegate instance for closure execution.
     * <p>
     * This method provides access to the delegate object that will be set on cloned closures
     * during {@link #methodMissing(String, Object)} execution. By default, it returns this
     * instance, but can be overridden in subclasses to provide a different delegation target.
     * </p>
     * <p>
     * The returned delegate is used as the delegate, owner, and thisObject for all dynamically
     * invoked closures, ensuring proper scope resolution within the ActionDelegate pattern.
     * </p>
     *
     * @return the delegate instance to be used for closure execution (this instance by default)
     */
    DelegateExpando getDelegate() {
        return this
    }

    /**
     * Overrides property setting to accumulate closures in the function map for method overloading support.
     * <p>
     * When a closure is assigned to a property, it is added to the list of closures associated with that
     * property name in the function map, rather than replacing any existing closure. This accumulation
     * mechanism is the foundation for method overloading: each closure represents a different method
     * signature, and all are stored under the same method name.
     * </p>
     * <p>
     * The insertion order is preserved using a {@link LinkedHashMap}, ensuring that when multiple closures
     * match during method resolution, the first matching closure (earliest defined) is selected.
     * </p>
     * <p>
     * Non-closure values are delegated to the parent {@link Expando} implementation, which stores them
     * as regular dynamic properties accessible via standard property access syntax.
     * </p>
     *
     * @param name the property name; becomes the method name when value is a closure
     * @param value the value to set; if a {@link Closure}, it is accumulated in the function map;
     *              otherwise handled by parent Expando
     */
    void setProperty(String name, Object value) {
        if (value instanceof Closure) {
            functionMap.computeIfAbsent(name, { new ArrayList<>()}) << value
        } else {
            super.setProperty(name, value)
        }
    }

    /**
     * Intercepts method calls that don't exist on this object and attempts to execute matching closures.
     * <p>
     * This method implements the dynamic method invocation and overload resolution mechanism by:
     * <ol>
     *   <li>Normalizing the incoming arguments to a standard Object[] array via {@link #normalize(Object)}</li>
     *   <li>Looking up candidate closures in the function map by method name</li>
     *   <li>Iterating through candidates to find the first closure with matching parameter count and compatible types</li>
     *   <li>Cloning the matching closure to prevent modification of the original</li>
     *   <li>Rehydrating the clone with the result of {@link #getDelegate()} as its delegate</li>
     *   <li>Setting the closure's resolution strategy to {@link Closure#DELEGATE_FIRST} for proper scope resolution</li>
     *   <li>Executing the closure with the normalized arguments</li>
     * </ol>
     * </p>
     * <p>
     * Parameter matching is delegated to {@link ClosureSignatureMatcher#matches(Closure, Object [ ])}, which verifies
     * that the argument count matches and each argument type is assignable to the corresponding parameter type.
     * Null arguments are treated as compatible with any reference parameter type. The first matching closure in
     * insertion order is selected and executed.
     * </p>
     * <p>
     * <b>Closure Isolation:</b> Each method invocation receives a clone of the matched closure, ensuring that
     * modifications to delegate, resolveStrategy, or other closure properties don't affect subsequent calls.
     * </p>
     *
     * @param name the name of the method being called
     * @param args the arguments passed to the method; can be an Object[], List, or single value
     * @return the result of executing the matched closure
     * @throws MissingMethodException if no closure with the given name exists or no closure matches the argument signature
     */
    def methodMissing(String name, args) {
        Object[] normalizedArgs = normalize(args)

        def candidates = functionMap[name]
        if (candidates) {
            Closure match = candidates.find { fn ->
                ClosureSignatureMatcher.matches(fn, normalizedArgs)
            }
            if (match) {
                def instance = match.clone()
                ((Closure) instance).delegate = getDelegate()
                ((Closure) instance).resolveStrategy = Closure.DELEGATE_FIRST
                return ((Closure) instance).call(*normalizedArgs)
            }
        }
        throw new MissingMethodException(name, this.class, args as Object[])
    }

    /**
     * Normalizes method arguments into a consistent Object[] array format.
     * <p>
     * Groovy's method invocation can pass arguments in various forms depending on the call site.
     * This method ensures that arguments are always converted to an Object[] array for consistent
     * processing by {@link #methodMissing(String, Object)}.
     * </p>
     * <p>
     * Normalization rules:
     * <ul>
     *   <li>If args is already an Object[], it is returned as-is</li>
     *   <li>If args is a List, it is converted to an array via {@link List#toArray()}</li>
     *   <li>Otherwise, args is wrapped in a single-element Object[] array</li>
     * </ul>
     * </p>
     *
     * @param args the raw arguments passed from Groovy's method invocation mechanism
     * @return a normalized Object[] array containing the method arguments
     */
    private Object[] normalize(args) {
        if (args instanceof Object[]) {
            return args
        } else if (args instanceof List) {
            return args.toArray()
        }
        return [args] as Object[]
    }
}
