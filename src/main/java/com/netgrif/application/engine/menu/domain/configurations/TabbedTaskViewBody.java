package com.netgrif.application.engine.menu.domain.configurations;

import com.netgrif.application.engine.menu.domain.MenuItemView;
import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.workflow.domain.Case;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TabbedTaskViewBody extends ViewBody {
    private Case filter;
    private boolean mergeFilters = true;
    private String viewSearchType = "fulltext_advanced";
    private List<String> headersMode = new ArrayList<>(List.of("sort", "edit"));
    private String headersDefaultMode = "sort";
    private boolean isHeaderModeChangeable = true;
    private boolean allowHeaderTableMode = true;
    private boolean useDefaultHeaders = true;
    private List<String> defaultHeaders;
    private boolean showMoreMenu = true;

    @Override
    public ViewBody getAssociatedViewBody() {
        return null;
    }

    @Override
    public MenuItemView getViewType() {
        return MenuItemView.TABBED_TASK_VIEW;
    }

    @Override
    protected ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome) {

        outcome.putDataSetEntry(TabbedTaskViewConstants.FIELD_MERGE_FILTERS, FieldType.BOOLEAN,
                this.mergeFilters);
        outcome.putDataSetEntry(TabbedTaskViewConstants.FIELD_VIEW_SEARCH_TYPE, FieldType.ENUMERATION_MAP,
                this.viewSearchType);
        outcome.putDataSetEntry(TabbedTaskViewConstants.FIELD_HEADERS_MODE, FieldType.MULTICHOICE_MAP,
                this.headersMode == null ? new ArrayList<>() : this.headersMode);
        outcome.putDataSetEntry(TabbedTaskViewConstants.FIELD_HEADERS_DEFAULT_MODE, FieldType.ENUMERATION_MAP,
                this.headersDefaultMode);
        outcome.putDataSetEntry(TabbedTaskViewConstants.FIELD_IS_HEADER_MODE_CHANGEABLE, FieldType.BOOLEAN,
                this.isHeaderModeChangeable);
        outcome.putDataSetEntry(TabbedTaskViewConstants.FIELD_ALLOW_HEADER_TABLE_MODE, FieldType.BOOLEAN,
                this.allowHeaderTableMode);
        outcome.putDataSetEntry(TabbedTaskViewConstants.FIELD_USE_DEFAULT_HEADERS, FieldType.BOOLEAN,
                this.useDefaultHeaders);
        outcome.putDataSetEntry(TabbedTaskViewConstants.FIELD_DEFAULT_HEADERS, FieldType.TEXT,
                this.defaultHeaders != null ? String.join(",", this.defaultHeaders) : null);
        outcome.putDataSetEntry(TabbedTaskViewConstants.FIELD_SHOW_MORE_MENU, FieldType.BOOLEAN,
                this.showMoreMenu);

        return outcome;
    }
}
