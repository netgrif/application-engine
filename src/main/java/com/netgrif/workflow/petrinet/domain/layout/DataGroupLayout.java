package com.netgrif.workflow.petrinet.domain.layout;

import lombok.Data;
import com.netgrif.workflow.importer.model.DataGroup;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataGroupLayout extends FormLayout {

    public DataGroupLayout(Integer rows, Integer cols, String type, Boolean hideEmptyRows, String compactDirection) {
        super(rows, cols, type, hideEmptyRows, compactDirection);
    }

    public DataGroupLayout(DataGroup data) {
        super(
                data.getRows(),
                data.getCols(),
                data.getLayout() != null ? data.getLayout().value() : null,
                data.isHideEmptyRows(),
                data.getCompactDirection() != null ? data.getCompactDirection().value() : null
        );
    }
}
