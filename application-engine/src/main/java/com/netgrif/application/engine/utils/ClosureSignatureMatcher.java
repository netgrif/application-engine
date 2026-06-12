package com.netgrif.application.engine.utils;

import com.netgrif.application.engine.petrinet.domain.dataset.logic.action.FieldActionsRunner;
import groovy.lang.Closure;

import java.lang.reflect.Method;

/**
 * Utility class for matching Groovy Closure signatures against provided arguments.
 * <p>
 * This class provides functionality to determine whether a given Groovy {@link Closure} can accept
 * a specific set of arguments based on its method signature. It inspects the closure's {@code doCall}
 * methods and verifies if any of them match the provided argument types and count.
 * </p>
 * <p>
 * This is particularly useful when working with dynamic Groovy closures in a Java context,
 * where compile-time type checking is not available and runtime signature matching is required.
 * </p>
 *
 * @see Closure
 * @see FieldActionsRunner
 */
public class ClosureSignatureMatcher {

    /**
     * Checks whether the provided Groovy closure can accept the given arguments.
     * <p>
     * This method iterates through all methods of the closure's class, looking for methods named {@code doCall}.
     * For each {@code doCall} method found, it verifies:
     * <ul>
     *   <li>The parameter count matches the argument count</li>
     *   <li>Each argument's type is assignable to the corresponding parameter type</li>
     *   <li>Null arguments are allowed for any parameter type</li>
     * </ul>
     * </p>
     *
     * @param fn   the Groovy closure to check for signature compatibility
     * @param args the array of arguments to match against the closure's signature;
     *             null values in the array are considered compatible with any parameter type
     * @return {@code true} if at least one {@code doCall} method in the closure matches the provided arguments;
     * {@code false} otherwise
     */
    public static boolean matches(Closure<?> fn, Object[] args) {
        for (Method method : fn.getClass().getMethods()) {
            if (!method.getName().equals("doCall")) continue;
            Class<?>[] params = method.getParameterTypes();
            if (params.length != args.length) continue;
            boolean allMatch = true;
            for (int i = 0; i < params.length; i++) {
                if (args[i] != null && !params[i].isAssignableFrom(args[i].getClass())) {
                    allMatch = false;
                    break;
                }
            }
            if (allMatch) return true;
        }
        return false;
    }
}
