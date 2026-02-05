package com.netgrif.application.engine.adapter.spring.utils;

import com.netgrif.application.engine.adapter.spring.utils.exceptions.AmbiguousMethodCallException;
import org.springframework.aop.framework.AopProxyUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class NaeReflectionUtils {

    private NaeReflectionUtils() {
        throw new IllegalStateException("No instances. Utility class");
    }

    /**
     * Resolves and returns the {@code Class} object for the given input object.
     * If the input object is a {@code Class}, it is returned directly.
     * If the input object is an AOP proxy, the target class of the proxy is returned.
     * Otherwise, the class of the object is returned.
     *
     * @param <T>    the type of the input object
     * @param object the object for which the {@code Class} needs to be resolved
     * @return the {@code Class} object corresponding to the input object or null if null value provided
     */
    public static <T> Class<?> resolveClass(T object) {
        if (object == null) return null;
        if (object instanceof Class) return (Class<?>) object;
        return AopProxyUtils.ultimateTargetClass(object);
    }

    /**
     * Returns the index of the first occurrence of the specified class in the given list.
     * If the list contains an element whose class matches the specified class, the index of that element is returned.
     * If the specified class is {@code null}, the method returns the index of the first {@code null} element in the list.
     * If the list is {@code null} or empty, or if the class is not found, the method returns {@code -1}.
     *
     * @param <I>   the type of elements in the list
     * @param list  the list to search for the specified class
     * @param clazz the class to search for in the list
     * @return the index of the first occurrence of the specified class in the list, or {@code -1} if the class is not found
     */
    public static <I> int indexOfClass(List<I> list, Class<?> clazz) {
        if (list == null) return -1;
        if (list.isEmpty()) return -1;
        if (clazz == null) return list.indexOf(null);
        for (int i = 0; i < list.size(); i++) {
            I item = list.get(i);
            if (item == null) continue;
            Class<?> itemClass = resolveClass(item);
            if (Objects.equals(itemClass, clazz)) {
                return i;
            }
        }
        return -1;
    }


    /**
     * Finds and returns the {@code Method} object for the specified method name and parameter types on the given bean.
     * This method first attempts to locate the method on the target class of the bean.
     * If not found, it tries to locate a method using superclasses of the parameter types.
     *
     * @param bean              the bean instance on which the method is to be located
     * @param methodToExecute   the name of the method to be invoked
     * @param requestParamTypes the types of the parameters expected by the method
     * @return the {@code Method} object corresponding to the specified name and parameters
     * @throws NoSuchMethodException        if the method cannot be found
     * @throws AmbiguousMethodCallException if multiple methods match the specified criteria, resulting in ambiguity
     */
    public static Method findMethod(Object bean, String methodToExecute, Class<?>[] requestParamTypes)
            throws NoSuchMethodException, AmbiguousMethodCallException {
        Objects.requireNonNull(bean, "bean must not be null");
        Objects.requireNonNull(methodToExecute, "methodToExecute must not be null");
        final Class<?>[] paramTypes = (requestParamTypes == null) ? new Class<?>[0] : requestParamTypes;
        try {
            return NaeReflectionUtils.resolveClass(bean).getMethod(methodToExecute, paramTypes);
        } catch (NoSuchMethodException e) {
            return findMethodWithSuperClassParams(bean, methodToExecute, requestParamTypes, e);
        }
    }

    /**
     * Attempts to find a method on the target class of the given bean using superclass parameter types when an exact match is not found.
     * If a method with the specified name is located, and the parameters can be assigned from the provided parameter types,
     * the method is returned. If multiple methods match the criteria, an {@code AmbiguousMethodCallException} is thrown.
     *
     * @param bean              the bean instance on which the method is to be located
     * @param methodToExecute   the name of the method to be invoked
     * @param requestParamTypes the types of the parameters expected by the method
     * @param caughtException   the exception caught from a previous attempt to find the method (used for re-throw if no method is found)
     * @return the {@code Method} object that matches the specified criteria
     * @throws NoSuchMethodException        if no suitable method is found
     * @throws AmbiguousMethodCallException if multiple methods match the specified name and parameters, causing ambiguity
     */
    private static Method findMethodWithSuperClassParams(Object bean, String methodToExecute, Class<?>[] requestParamTypes,
                                                  NoSuchMethodException caughtException)
            throws NoSuchMethodException, AmbiguousMethodCallException {
        Class<?> cls = NaeReflectionUtils.resolveClass(bean);
        Method[] methods = Arrays.stream(cls.getMethods())
                .filter(m -> !m.isBridge() && !m.isSynthetic())
                .toArray(Method[]::new);
        Method methodToInvoke = null;
        outerLoop:
        for (Method method : methods) {
            if (!methodToExecute.equals(method.getName())) {
                continue;
            }

            Class<?>[] paramTypes = method.getParameterTypes();
            int requestParamsLen = requestParamTypes.length;
            int paramsLen = paramTypes.length;

            if (requestParamsLen == 0 && paramsLen == 0) {
                methodToInvoke = method;
                break;
            } else if (paramsLen == 0 || paramsLen != requestParamsLen) {
                continue;
            }

            for (int i = 0; i < requestParamTypes.length; ++i) {
                if (!paramTypes[i].isAssignableFrom(requestParamTypes[i])) {
                    continue outerLoop;
                }
            }

            if (methodToInvoke != null) {
                throw new AmbiguousMethodCallException(String.format("Method %s is ambiguous for the param types %s",
                        methodToExecute, Arrays.toString(requestParamTypes)));
            }
            methodToInvoke = method;
        }

        if (methodToInvoke == null) {
            throw caughtException;
        } else {
            return methodToInvoke;
        }
    }
}
