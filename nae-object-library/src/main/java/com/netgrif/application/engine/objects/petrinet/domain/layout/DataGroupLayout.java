package com.netgrif.application.engine.objects.petrinet.domain.layout;

import com.netgrif.application.engine.objects.importer.model.DataGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DataGroupLayout extends FormLayout {

    public DataGroupLayout(Integer rows, Integer cols, String type, String hideEmptyRows, String compactDirection) {
        super(rows, cols, type, hideEmptyRows, compactDirection);
    }

    public DataGroupLayout(DataGroup data) {
        super(
                data.getRows(),
                data.getCols(),
                data.getLayout() != null ? data.getLayout().value() : null,
                data.getHideEmptyRows() != null ? data.getHideEmptyRows().value() : null,
                data.getCompactDirection() != null ? data.getCompactDirection().value() : null
        );
    }
}
