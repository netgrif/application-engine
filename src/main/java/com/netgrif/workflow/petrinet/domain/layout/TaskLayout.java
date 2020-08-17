package com.netgrif.workflow.petrinet.domain.layout;

import com.netgrif.workflow.importer.model.Transition;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskLayout extends Layout {

    private Integer offset;

    public TaskLayout(Transition data) {
        super(data.getLayout().getRows(), data.getLayout().getCols());
        this.offset = data.getLayout().getOffset();
    }
}
