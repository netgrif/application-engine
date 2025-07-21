package com.netgrif.application.engine.objects.workflow.domain.menu.configurations;

import com.netgrif.application.engine.objects.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemView;
import com.netgrif.application.engine.objects.workflow.domain.menu.ToDataSetOutcome;
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
    public MenuItemView getViewType() {
        return MenuItemView.TABBED_SINGLE_TASK_VIEW;
    }

    @Override
    protected ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome) {
        outcome.putDataSetEntry(TabbedSingleTaskViewConstants.FIELD_TRANSITION_ID, FieldType.TEXT, this.transitionId);
        return outcome;
    }
}
