package com.netgrif.application.engine.petrinet.domain.layout;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class FormLayout extends Layout {

    private String type;
    private String hideEmptyRows;
    private String compactDirection;

    public FormLayout(Integer rows, Integer cols, String type, String hideEmptyRows, String compactDirection) {
        super(rows, cols);
        this.type = type;
        this.hideEmptyRows = hideEmptyRows;
        this.compactDirection = compactDirection;
    }
}
