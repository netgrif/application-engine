package com.netgrif.application.engine.petrinet.domain.layout;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.netgrif.application.engine.mapper.filters.ColFilter;
import com.netgrif.application.engine.mapper.filters.RowFilter;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Layout {

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = RowFilter.class)
    protected Integer rows;

    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = ColFilter.class)
    protected Integer cols;

    public Layout(Integer rows, Integer cols) {
        this.rows = rows == null || rows == 0 ? null : rows;
        this.cols = cols == null || cols == 0 ? null : cols;
    }

    public boolean hasNonDefaultAttribute() {
        return !rows.equals(1) || !cols.equals(2);
    }
}
