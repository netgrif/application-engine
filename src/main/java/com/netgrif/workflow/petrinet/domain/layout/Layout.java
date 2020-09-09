package com.netgrif.workflow.petrinet.domain.layout;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public abstract class Layout {

    private Integer rows;
    private Integer cols;

    public Layout(Integer rows, Integer cols) {
        this.rows = rows == null || rows == 0 ? null : rows;
        this.cols = cols == null || cols == 0 ? null : cols;
    }

    public Layout() {
    }
}
