package com.netgrif.workflow.petrinet.domain.layout;

import com.netgrif.workflow.importer.model.Transition;
import lombok.Getter;
import lombok.Setter;

public class TaskLayout extends Layout {

    @Getter
    @Setter
    private Integer offset;

    public TaskLayout(Integer rows, Integer cols, Integer offset) {
        super(rows, cols);
        this.offset = offset;
    }

    public TaskLayout(Transition data) {
        super(data.getLayout().getRows(), data.getLayout().getCols());
        this.offset = data.getLayout().getOffset();
    }

    public TaskLayout() {
        super();
    }
}
