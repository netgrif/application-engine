package com.netgrif.workflow.workflow.domain;

import lombok.Data;

@Data
public class WrappingLayout {
    private int wrapping;

    public WrappingLayout(int wrapping) {
        this.wrapping = wrapping;
    }
}
