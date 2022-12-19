package com.netgrif.application.engine.petrinet.domain.layout;

import com.netgrif.application.engine.importer.model.FieldAlignment;
import com.netgrif.application.engine.importer.model.Transition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskLayout extends FormLayout {

    private Integer offset;
    private FieldAlignment fieldAlignment;

    public TaskLayout(Transition data) {
        super(
                data.getLayout().getRows(),
                data.getLayout().getCols(),
                data.getLayout().getType() != null ? data.getLayout().getType() : null,
                data.getLayout().getHideEmptyRows() != null ? data.getLayout().getHideEmptyRows() : null,
                data.getLayout().getCompactDirection() != null ? data.getLayout().getCompactDirection() : null
        );
        this.offset = data.getLayout().getOffset();
        this.fieldAlignment = data.getLayout().getFieldAlignment() != null ? data.getLayout().getFieldAlignment() : null;
    }
}
