package com.netgrif.application.engine.petrinet.domain.layout;

import com.netgrif.application.engine.importer.model.CompactDirection;
import com.netgrif.application.engine.importer.model.HideEmptyRows;
import com.netgrif.application.engine.importer.model.LayoutType;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class FormLayout extends Layout {
    // TODO: NAE-1645 clean up querydsl annotations
    @QueryType(PropertyType.NONE)
    private LayoutType type;
    @QueryType(PropertyType.NONE)
    private HideEmptyRows hideEmptyRows;
    @QueryType(PropertyType.NONE)
    private CompactDirection compactDirection;

    public FormLayout(Integer rows, Integer cols, LayoutType type, HideEmptyRows hideEmptyRows, CompactDirection compactDirection) {
        super(rows, cols);
        this.type = type;
        this.hideEmptyRows = hideEmptyRows;
        this.compactDirection = compactDirection;
    }
}
