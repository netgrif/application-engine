package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.functions

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate


/**
 * A specialized Expando implementation that provides memory-efficient attachment of global scoped functions
 * from business processes to an {@link ActionDelegate}.
 * <p>
 * This class solves a critical memory optimization problem in the execution of process actions. Global functions
 * defined in processes need to be accessible during action execution, but storing fully rehydrated closures
 * for every function would consume significant heap space, especially in systems with many processes and functions.
 * </p>
 * <p>
 * Instead of rehydrating all global functions for each action execution, this class acts as a lightweight proxy
 * that:
 * <ul>
 *   <li>Stores function closures as properties on the Expando metaclass</li>
 *   <li>Delegates method calls to either stored function closures or the parent ActionDelegate</li>
 *   <li>Delegates property access to the parent ActionDelegate</li>
 *   <li>Minimizes heap usage by avoiding unnecessary closure rehydration</li>
 * </ul>
 * </p>
 * <p>
 * The typical usage pattern is:
 * <ol>
 *   <li>Global functions are cached as closures in {@link com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService}</li>
 *   <li>During action execution, a FunctionExpando instance is created for each scope (namespace) of global functions</li>
 *   <li>Function closures are attached to the FunctionExpando's metaclass</li>
 *   <li>When a function is invoked, methodMissing intercepts the call and executes the cached closure</li>
 *   <li>When a property is accessed, propertyMissing delegates to the parent ActionDelegate</li>
 * </ol>
 * </p>
 * <p>
 * This design allows global functions to be shared across multiple action executions without rehydration,
 * significantly reducing memory footprint and improving performance.
 * </p>
 *
 * @see ActionDelegate* @see com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService* @see com.netgrif.application.engine.workflow.domain.CachedFunction
 */
class FunctionExpando extends Expando {

    /**
     * Reference to the parent ActionDelegate that provides the execution context for actions.
     * <p>
     * This delegate is used to:
     * <ul>
     *   <li>Access case and task data during function execution</li>
     *   <li>Delegate method calls that are not function invocations</li>
     *   <li>Delegate all property read and write operations</li>
     *   <li>Provide access to the full action execution environment</li>
     * </ul>
     * </p>
     * <p>
     * By maintaining this reference, the FunctionExpando can act as a transparent proxy that adds
     * function invocation capabilities while preserving all original ActionDelegate functionality.
     * </p>
     */
    ActionDelegate parentDelegate

    /**
     * Constructs a new FunctionExpando attached to the specified ActionDelegate.
     * <p>
     * The constructor establishes the delegation relationship that allows this FunctionExpando
     * to act as a proxy for both function invocations and property access.
     * </p>
     *
     * @param parentDelegate the ActionDelegate that provides the execution context and handles
     *                      non-function method calls and all property access
     */
    FunctionExpando(ActionDelegate parentDelegate) {
        this.parentDelegate = parentDelegate
    }

    /**
     * Intercepts method calls to provide dynamic resolution of function invocations.
     * <p>
     * This method implements Groovy's dynamic method resolution mechanism to handle calls
     * that are not statically defined on this class. It follows this resolution strategy:
     * <ol>
     *   <li>Check if a property with the method name exists and is a Closure (cached function)</li>
     *   <li>If found, rehydrate the closure with the parent ActionDelegate as delegate, owner, and thisObject</li>
     *   <li>Set the rehydrated closure's resolve strategy to DELEGATE_FIRST to prioritize ActionDelegate properties</li>
     *   <li>Invoke the rehydrated closure with the provided arguments</li>
     *   <li>Otherwise, delegate the method call to the parent ActionDelegate</li>
     * </ol>
     * </p>
     * <p>
     * This approach enables:
     * <ul>
     *   <li>Execution of cached global functions with proper ActionDelegate context</li>
     *   <li>Access to case and task data from within function closures</li>
     *   <li>Transparent fallback to ActionDelegate methods</li>
     *   <li>Seamless integration of global functions into the action execution context</li>
     * </ul>
     * </p>
     *
     * @param name the name of the method being invoked
     * @param args the arguments passed to the method (typically an Object array)
     * @return the result of the function or method invocation
     * @throws groovy.lang.MissingMethodException if neither a cached function nor a delegate method exists
     */
    def methodMissing(String name, args) {
        def fn = getProperties()[name]
        if (fn instanceof Closure) {
            def rehydratedClosure = fn.rehydrate(parentDelegate, parentDelegate, parentDelegate)
            rehydratedClosure.resolveStrategy = Closure.DELEGATE_FIRST
            return rehydratedClosure.call(args)
        }
        return parentDelegate.invokeMethod(name, args)
    }
    
    /**
     * Intercepts property read access with a two-tier resolution strategy.
     * <p>
     * This method handles dynamic property resolution for read operations using the following strategy:
     * <ol>
     *   <li>First, attempts to retrieve the property from this FunctionExpando instance using subscript notation</li>
     *   <li>If the property is null or doesn't exist (evaluates to false), delegates to the parent ActionDelegate using the Elvis operator</li>
     * </ol>
     * </p>
     * <p>
     * This two-tier approach ensures that:
     * <ul>
     *   <li>Cached function closures stored on FunctionExpando are directly accessible</li>
     *   <li>Case and task data fields from ActionDelegate are accessible when not shadowed</li>
     *   <li>ActionDelegate properties and variables are available as a fallback</li>
     *   <li>The FunctionExpando acts as a transparent proxy with function overlay capabilities</li>
     *   <li>Null or falsy values on FunctionExpando cause automatic fallback to parent delegate</li>
     * </ul>
     * </p>
     *
     * @param name the name of the property being accessed
     * @return the value of the property from this FunctionExpando if it exists and is truthy, otherwise from the parent ActionDelegate
     * @throws groovy.lang.MissingPropertyException if the property does not exist on either this instance or the parent delegate
     */
    def propertyMissing(String name) {
        this[name] ?: parentDelegate."${name}"
    }

    /**
     * Intercepts property write access with conditional storage strategy for Closures.
     * <p>
     * This method handles dynamic property resolution for write operations with special handling
     * for Closure values (cached functions). The behavior is:
     * <ol>
     *   <li>If the value is a Closure, it is stored on this FunctionExpando instance for efficient function caching</li>
     *   <li>If the value is NOT a Closure, the property assignment is delegated to the parent ActionDelegate only</li>
     * </ol>
     * </p>
     * <p>
     * This conditional storage approach ensures that:
     * <ul>
     *   <li>Function closures are stored exclusively on FunctionExpando for fast access via propertyMissing(String) and methodMissing</li>
     *   <li>Non-Closure values (case/task data fields, variables) are stored only on ActionDelegate to avoid duplication</li>
     *   <li>ActionDelegate variables can be set normally without shadowing on FunctionExpando</li>
     *   <li>The FunctionExpando maintains both its function caching capability and transparent proxy behavior</li>
     * </ul>
     * </p>
     * <p>
     * Note: Unlike the dual storage approach, Closure values are stored ONLY on this FunctionExpando instance,
     * while non-Closure values are stored ONLY on the parent ActionDelegate, preventing unnecessary duplication
     * and ensuring proper separation of concerns between function storage and data storage.
     * </p>
     *
     * @param name the name of the property being set
     * @param value the value to assign to the property; Closures are stored locally, all other values are delegated
     * @throws groovy.lang.MissingPropertyException if the property cannot be set on the parent delegate
     */
    def propertyMissing(String name, value) {
        if (value instanceof Closure) {
            this."${name}" = value
        }
        parentDelegate."${name}" = value
    }
}
