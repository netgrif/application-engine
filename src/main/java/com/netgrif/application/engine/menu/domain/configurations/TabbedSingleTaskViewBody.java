package com.netgrif.application.engine.menu.domain.configurations;

import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.startup.MenuItemViewRegistryRunner;
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
    public String getViewIdentifier() {
        return MenuItemViewRegistryRunner.TABBED_SINGLE_TASK_VIEW_ID;
    }

    @Override
    protected ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome) {
        outcome.putDataSetEntry(TabbedSingleTaskViewConstants.FIELD_TRANSITION_ID, FieldType.TEXT, this.transitionId);
        return outcome;
    }
}
