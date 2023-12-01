package com.netgrif.application.engine.petrinet.domain.layout;

import com.netgrif.application.engine.importer.model.FieldAlignment;
import com.netgrif.application.engine.importer.model.Transition;
import com.querydsl.core.annotations.PropertyType;
import com.querydsl.core.annotations.QueryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskLayout extends FormLayout {

    private Integer offset;
    @QueryType(PropertyType.NONE)
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
