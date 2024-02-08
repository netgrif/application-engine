package com.netgrif.application.engine.petrinet.domain.layout;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public abstract class Layout implements Serializable {

    private static final long serialVersionUID = -1177510908437276099L;

    private Integer rows;
    private Integer cols;

    public Layout(Integer rows, Integer cols) {
        this.rows = rows == null || rows == 0 ? null : rows;
        this.cols = cols == null || cols == 0 ? null : cols;
    }
}
