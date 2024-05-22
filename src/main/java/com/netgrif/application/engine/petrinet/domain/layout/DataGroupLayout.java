package com.netgrif.application.engine.petrinet.domain.layout;

import com.netgrif.application.engine.importer.model.CompactDirection;
import com.netgrif.application.engine.importer.model.HideEmptyRows;
import com.netgrif.application.engine.importer.model.LayoutType;
import lombok.Data;
import com.netgrif.application.engine.importer.model.DataGroup;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataGroupLayout extends FormLayout {

    public DataGroupLayout(Integer rows, Integer cols, LayoutType type, HideEmptyRows hideEmptyRows, CompactDirection compactDirection) {
        super(rows, cols, type, hideEmptyRows, compactDirection);
    }

    public DataGroupLayout(DataGroup data) {
        super(
                data.getRows(),
                data.getCols(),
                data.getLayout() != null ? data.getLayout() : null,
                data.getHideEmptyRows() != null ? data.getHideEmptyRows() : null,
                data.getCompactDirection() != null ? data.getCompactDirection() : null
        );
    }
}
