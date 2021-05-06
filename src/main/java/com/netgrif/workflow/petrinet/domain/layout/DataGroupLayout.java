package com.netgrif.workflow.petrinet.domain.layout;

import lombok.Data;

@Data
public class DataGroupLayout extends Layout {

    private String type;

    public DataGroupLayout(Integer rows, Integer cols, String type) {
        super(rows, cols);
        this.type = type;
    }

    public DataGroupLayout() {
        super();
    }
}
