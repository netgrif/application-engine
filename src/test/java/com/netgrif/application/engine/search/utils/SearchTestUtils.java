package com.netgrif.application.engine.search.utils;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class SearchTestUtils {

    public static <T> T convertToObject(Object object, Class<T> targetClass) {
        assert targetClass.isInstance(object);
        return targetClass.cast(object);
    }

    public static <T> List<T> convertToObjectList(Object objectList, Class<T> targetClass) {
        assert objectList instanceof List<?>;
        for (Object object : (List<?>) objectList) {
            assert targetClass.isInstance(object);
        }

        return (List<T>) objectList;
    }

    public static <T> void compareById(T actual, T expected, Function<T, String> getId) {
        assert getId.apply(actual).equals(getId.apply(expected));
    }

    public static <T> void compareById(T actual, List<T> expected, Function<T, String> getId) {
        List<String> expectedIds = expected.stream()
                .map(getId)
                .collect(Collectors.toList());

        assert expectedIds.contains(getId.apply(actual));
    }

    public static <T> void compareById(List<T> actual, List<T> expected, Function<T, String> getId) {
        List<String> actualIds = actual.stream()
                .map(getId)
                .collect(Collectors.toList());
        List<String> expectedIds = expected.stream()
                .map(getId)
                .collect(Collectors.toList());

        assert actualIds.containsAll(expectedIds);
    }

    public static <T> void compareByIdInOrder(List<T> actual, List<T> expected, Function<T, String> getId) {
        List<String> actualIds = actual.stream()
                .map(getId)
                .collect(Collectors.toList());
        List<String> expectedIds = expected.stream()
                .map(getId)
                .collect(Collectors.toList());

        assert actualIds.equals(expectedIds);

        int lastIndex = -1;
        for (String expectedId : expectedIds) {
            int currentIndex = actualIds.indexOf(expectedId);
            assert currentIndex > lastIndex;
            lastIndex = currentIndex;
        }
    }
}
