package com.netgrif.application.engine.objects.utils;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

@Slf4j
public class CopyConstructorUtil{

    public static <T> T copy(Class<? extends T> type, T toBeCopied) {
        try {
            return type.getDeclaredConstructor(type).newInstance(toBeCopied);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            log.error("Failed to copy object of type {}", type.getName(), e);
            throw new RuntimeException(e);
        }
    }
}
