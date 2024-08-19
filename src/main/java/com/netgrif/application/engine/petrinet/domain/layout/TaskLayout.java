package com.netgrif.application.engine.petrinet.domain.layout;

import com.netgrif.application.engine.importer.model.Transition;
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
                data.getLayout().getHideEmptyRows() != null ? data.getLayout().getHideEmptyRows().value() : null,
                data.getLayout().getCompactDirection() != null ? data.getLayout().getCompactDirection().value() : null
        );
        this.offset = data.getLayout().getOffset();
        this.fieldAlignment = data.getLayout().getFieldAlignment() != null ? data.getLayout().getFieldAlignment().value() : null;
    }

    @Override
    public TaskLayout clone() {
        TaskLayout clone = new TaskLayout();
        clone.setCols(this.getCols());
        clone.setRows(this.getRows());
        clone.setType(this.getType());
        clone.setOffset(this.offset);
        clone.setFieldAlignment(this.fieldAlignment);
        clone.setCompactDirection(this.getCompactDirection());
        clone.setHideEmptyRows(this.getHideEmptyRows());
        return clone;
    }
}
