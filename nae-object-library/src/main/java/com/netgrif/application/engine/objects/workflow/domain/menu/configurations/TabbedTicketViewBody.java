package com.netgrif.application.engine.objects.workflow.domain.menu.configurations;

import com.netgrif.application.engine.objects.workflow.domain.menu.MenuItemView;
import com.netgrif.application.engine.objects.workflow.domain.menu.ToDataSetOutcome;
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
    public MenuItemView getViewType() {
        return MenuItemView.TABBED_TICKET_VIEW;
    }

    @Override
    protected ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome) {
        return outcome;
    }
}
