package com.netgrif.workflow.petrinet.domain.layout;

import com.netgrif.workflow.importer.model.Transition;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskLayout extends Layout {

    private Integer offset;
    private String type;
    private String fieldAlignment;

    public TaskLayout(Transition data) {
        super(data.getLayout().getRows(), data.getLayout().getCols());
        this.offset = data.getLayout().getOffset();
        this.type = data.getLayout().getType() != null ? data.getLayout().getType().value() : null;
        this.fieldAlignment = data.getLayout().getFieldAlignment() != null ? data.getLayout().getFieldAlignment().value() : null;
    }
}
