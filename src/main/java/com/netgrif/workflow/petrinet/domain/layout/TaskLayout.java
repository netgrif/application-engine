package com.netgrif.workflow.petrinet.domain.layout;

import com.netgrif.workflow.importer.model.Transition;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskLayout extends FormLayout {

    private Integer offset;
    private String fieldAlignment;

    public TaskLayout(Transition data) {
        super(
                data.getLayout().getRows(),
                data.getLayout().getCols(),
                data.getLayout().getType() != null ? data.getLayout().getType().value() : null,
                data.getLayout().isHideEmptyRows(),
                data.getLayout().getCompactDirection() != null ? data.getLayout().getCompactDirection().value() : null
        );
        this.offset = data.getLayout().getOffset();
        this.fieldAlignment = data.getLayout().getFieldAlignment() != null ? data.getLayout().getFieldAlignment().value() : null;
    }
}
