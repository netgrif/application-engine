package com.netgrif.application.engine.petrinet.domain.layout;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class FormLayout extends Layout {

    // TODO: NAE-1645 enumerations, https://engine.netgrif.com/#/views/form_layout
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
