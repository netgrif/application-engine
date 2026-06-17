package com.netgrif.application.engine.adapter.spring.utils;

import org.springframework.data.domain.*;

import java.util.List;

public class PageableUtils {

    public static <T> Page<T> listToPage(List<T> toConvert, Pageable pageable) {
        int page = pageable.getPageNumber();
        int size = pageable.getPageSize();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), toConvert.size());

        List<T> pageContent = toConvert.subList(start, end);
        return new PageImpl<>(pageContent, pageable, toConvert.size());
    }

    public static PageRequest fullPageRequest() {
        return new FullPageRequest();
    }

    private static class FullPageRequest extends PageRequest {
        public FullPageRequest() {
            super(0, Integer.MAX_VALUE, Sort.unsorted());
        }
    }
}
