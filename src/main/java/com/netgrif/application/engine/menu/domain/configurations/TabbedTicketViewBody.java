package com.netgrif.application.engine.menu.domain.configurations;

import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.startup.MenuItemViewRegistryRunner;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TabbedTicketViewBody extends ViewBody {

    private ViewBody chainedView;

    @Override
    public ViewBody getAssociatedViewBody() {
        return this.chainedView;
    }

    @Override
    public String getViewIdentifier() {
        return MenuItemViewRegistryRunner.TABBED_TICKET_VIEW_ID;
    }

    @Override
    protected ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome) {
        return outcome;
    }
}
