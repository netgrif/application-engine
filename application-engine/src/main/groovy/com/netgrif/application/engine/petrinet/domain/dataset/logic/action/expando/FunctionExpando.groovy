package com.netgrif.application.engine.petrinet.domain.dataset.logic.action.expando

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.ActionDelegate
import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner
import com.netgrif.application.engine.workflow.service.interfaces.IFieldActionsCacheService

/**
 * A specialized {@link DelegateExpando} implementation that provides memory-efficient organization of global scoped
 * functions from business processes by namespace.
 * <p>
 * This class serves as a namespace container for global functions defined in business processes. It extends
 * {@link DelegateExpando} to inherit property-based function storage capabilities while adding delegation
 * to a parent {@link ActionDelegate} for non-function method calls.
 * </p>
 * <p>
 * Key characteristics:
 * <ul>
 *   <li>Stores function closures as properties (inherited from {@link DelegateExpando})</li>
 *   <li>Provides namespace-based organization of global functions (e.g., process-specific function scopes)</li>
 *   <li>Delegates unresolved method calls to the parent {@link ActionDelegate}</li>
 *   <li>Enables clean separation of global functions by scope while maintaining access to {@link ActionDelegate} context</li>
 * </ul>
 * </p>
 * <p>
 * Typical usage in {@link FieldActionsRunner}:
 * <ol>
 *   <li>Global functions are cached as closures in {@link IFieldActionsCacheService}, grouped by namespace</li>
 *   <li>During action execution, a {@code FunctionExpando} instance is created for each namespace of global functions</li>
 *   <li>Function closures are attached to the {@code FunctionExpando} as properties (e.g., {@code functionExpando.myFunc = closure})</li>
 *   <li>The {@code FunctionExpando} instance is attached to the {@link ActionDelegate} (e.g., {@code actionDelegate.namespace = functionExpando})</li>
 *   <li>Functions can be invoked via namespace (e.g., {@code namespace.myFunc()} in action code)</li>
 *   <li>{@link DelegateExpando}'s {@code methodMissing} handles function invocation by rehydrating closures with {@link ActionDelegate} context</li>
 *   <li>This class's {@code methodMissing} handles fallback to {@link ActionDelegate} for non-function calls</li>
 * </ol>
 * </p>
 * <p>
 * This design enables logical grouping of global functions while ensuring they have full access to the
 * {@link ActionDelegate} execution environment.
 * </p>
 *
 * @see ActionDelegate* @see DelegateExpando* @see IFieldActionsCacheService* @see FieldActionsRunner
 */
class FunctionExpando extends DelegateExpando {

    /**
     * Reference to the parent {@link ActionDelegate} that provides the execution context for actions.
     * <p>
     * This delegate is used for:
     * <ul>
     *   <li>Fallback delegation of method calls that are not stored as function closures</li>
     *   <li>Providing context to function closures through {@link DelegateExpando}'s rehydration mechanism</li>
     *   <li>Enabling functions to access case data, task data, and other {@link ActionDelegate} capabilities</li>
     * </ul>
     * </p>
     * <p>
     * Note: Property access is handled by the parent {@link DelegateExpando} class, not by direct delegation
     * to this field.
     * </p>
     */
    ActionDelegate parentDelegate

    /**
     * Constructs a new {@code FunctionExpando} associated with the specified {@link ActionDelegate}.
     * <p>
     * The constructor establishes the delegation relationship that enables this {@code FunctionExpando}
     * to fall back to the {@link ActionDelegate} for method calls that are not resolved as function closures.
     * </p>
     *
     * @param parentDelegate the {@link ActionDelegate} that provides the execution context for functions
     *                       and handles non-function method calls
     */
    FunctionExpando(ActionDelegate parentDelegate) {
        this.parentDelegate = parentDelegate
    }

    /**
     * Returns the parent delegate that provides the execution context.
     * <p>
     * This method is overridden from {@link DelegateExpando} to return the {@link ActionDelegate}
     * instance, enabling proper delegation and context provision for function closures.
     * </p>
     *
     * @return the parent {@link ActionDelegate} instance
     */
    @Override
    DelegateExpando getDelegate() {
        return parentDelegate
    }

    /**
     * Intercepts method calls to provide two-level dynamic resolution: functions first, then {@link ActionDelegate}.
     * <p>
     * This method implements Groovy's dynamic method resolution mechanism to handle calls
     * that are not statically defined on this class. It follows this resolution strategy:
     * <ol>
     *   <li>Delegate to parent {@link DelegateExpando}'s {@code methodMissing} to check for function closures stored as properties</li>
     *   <li>{@link DelegateExpando} rehydrates found closures with {@code parentDelegate} as context and invokes them</li>
     *   <li>If no function closure is found ({@code MissingMethodException}), fall back to {@code parentDelegate.invokeMethod()}</li>
     *   <li>If neither resolution succeeds, propagate the {@code MissingMethodException}</li>
     * </ol>
     * </p>
     * <p>
     * This two-level approach enables:
     * <ul>
     *   <li>Execution of namespaced global functions with full {@link ActionDelegate} context</li>
     *   <li>Access to case and task data from within function closures (via {@link DelegateExpando}'s rehydration)</li>
     *   <li>Transparent fallback to {@link ActionDelegate} methods when no function matches</li>
     *   <li>Clean separation of namespace-scoped functions from {@link ActionDelegate} methods</li>
     * </ul>
     * </p>
     *
     * @param name the name of the method being invoked
     * @param args the arguments passed to the method (typically an {@code Object} array)
     * @return the result of the function or method invocation
     * @throws groovy.lang.MissingMethodException if neither a function closure nor a delegate method exists
     */
    def methodMissing(String name, args) {
        try {
            return super.methodMissing(name, args)  // try own functions first
        } catch (MissingMethodException ignored) {
            return parentDelegate.invokeMethod(name, args)  // fallback to delegate
        }
    }
}
