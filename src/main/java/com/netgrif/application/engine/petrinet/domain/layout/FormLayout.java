package com.netgrif.application.engine.petrinet.domain.layout;

import com.netgrif.application.engine.importer.model.CompactDirection;
import com.netgrif.application.engine.importer.model.HideEmptyRows;
import com.netgrif.application.engine.importer.model.LayoutType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class FormLayout extends Layout {

    private LayoutType type;
    private HideEmptyRows hideEmptyRows;
    private CompactDirection compactDirection;

    public FormLayout(Integer rows, Integer cols, LayoutType type, HideEmptyRows hideEmptyRows, CompactDirection compactDirection) {
        super(rows, cols);
        this.type = type;
        this.hideEmptyRows = hideEmptyRows;
        this.compactDirection = compactDirection;
    }
}
