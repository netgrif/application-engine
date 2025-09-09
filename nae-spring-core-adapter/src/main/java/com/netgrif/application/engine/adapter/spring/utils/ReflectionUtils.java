package com.netgrif.application.engine.adapter.spring.utils;

import org.springframework.aop.support.AopUtils;

import java.util.List;

public class ReflectionUtils {

    /**
     * Resolves and returns the {@code Class} object for the given input object.
     * If the input object is a {@code Class}, it is returned directly.
     * If the input object is an AOP proxy, the target class of the proxy is returned.
     * Otherwise, the class of the object is returned.
     *
     * @param <T> the type of the input object
     * @param object the object for which the {@code Class} needs to be resolved
     * @return the {@code Class} object corresponding to the input object
     */
    public static <T> Class<?> resolveClass(T object) {
        if (object instanceof Class) return (Class<?>) object;
        else return AopUtils.isAopProxy(object) ? AopUtils.getTargetClass(object) : object.getClass();
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
            if (resolveClass(list.get(i)).equals(clazz)) {
                return i;
            }
        }
        return -1;
    }




}
