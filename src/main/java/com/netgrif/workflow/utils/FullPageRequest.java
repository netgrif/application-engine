package com.netgrif.workflow.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

public class FullPageRequest extends PageRequest {

    public FullPageRequest() {
        super(0, Integer.MAX_VALUE, Sort.unsorted());
    }
}