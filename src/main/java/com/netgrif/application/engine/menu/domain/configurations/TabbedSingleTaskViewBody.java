package com.netgrif.application.engine.menu.domain.configurations;

import com.netgrif.application.engine.menu.domain.MenuItemViewOLD;
import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TabbedSingleTaskViewBody extends ViewBody {
    private String transitionId;

    @Override
    public ViewBody getAssociatedViewBody() {
        return null;
    }

    @Override
    public MenuItemViewOLD getViewType() {
        return MenuItemViewOLD.TABBED_SINGLE_TASK_VIEW;
    }

    @Override
    protected ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome) {
        outcome.putDataSetEntry(TabbedSingleTaskViewConstants.FIELD_TRANSITION_ID, FieldType.TEXT, this.transitionId);
        return outcome;
    }
}
