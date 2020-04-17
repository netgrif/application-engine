package com.netgrif.workflow.petrinet.domain.layout;

import lombok.Getter;
import lombok.Setter;

public abstract class Layout {
    @Getter @Setter
    private Integer rows;
    @Getter @Setter
    private Integer cols;

    public Layout(Integer rows, Integer cols) {
        this.rows = rows;
        this.cols = cols;
    }

    public Layout() {
    }
}
