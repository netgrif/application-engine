package com.netgrif.application.engine.menu.domain.configurations;

import com.netgrif.application.engine.menu.domain.MenuItemView;
import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SingleTaskViewBody extends ViewBody {
    private boolean showPageHeader = true;
    private boolean showPageFooter = false;

    @Override
    public ViewBody getAssociatedViewBody() {
        return null;
    }

    @Override
    public MenuItemView getViewType() {
        return MenuItemView.SINGLE_TASK_VIEW;
    }

    @Override
    protected ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome) {
        outcome.putDataSetEntry(SingleTaskViewConstants.FIELD_SHOW_PAGE_HEADER, FieldType.BOOLEAN, this.showPageHeader);
        outcome.putDataSetEntry(SingleTaskViewConstants.FIELD_SHOW_PAGE_FOOTER, FieldType.BOOLEAN, this.showPageFooter);
        return outcome;
    }
}
