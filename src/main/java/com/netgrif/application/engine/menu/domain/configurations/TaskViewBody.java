package com.netgrif.application.engine.menu.domain.configurations;

import com.netgrif.application.engine.menu.domain.MenuItemViewType;
import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TaskViewBody extends ViewBody {
    private boolean mergeFilters = true;
    private String viewSearchType = "fulltext_advanced";
    private List<String> headersMode = new ArrayList<>(List.of("sort", "edit"));
    private String headersDefaultMode = "sort";
    private boolean isHeaderModeChangeable = true;
    private boolean allowHeaderTableMode = true;
    private boolean useDefaultHeaders = true;
    private List<String> defaultHeaders;
    private boolean showMoreMenu = true;
    private I18nString emptyContentText;
    private String emptyContentIcon;

    @Override
    public ViewBody getAssociatedViewBody() {
        return null;
    }

    @Override
    public MenuItemViewType getViewType() {
        return MenuItemViewType.TASK_VIEW;
    }

    @Override
    public String getFilterFieldId() {
        return TaskViewConstants.FIELD_FILTER;
    }

    @Override
    protected ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome) {

        outcome.putDataSetEntry(TaskViewConstants.FIELD_MERGE_FILTERS, FieldType.BOOLEAN,
                this.mergeFilters);
        outcome.putDataSetEntry(TaskViewConstants.FIELD_VIEW_SEARCH_TYPE, FieldType.ENUMERATION_MAP,
                this.viewSearchType);
        outcome.putDataSetEntry(TaskViewConstants.FIELD_HEADERS_MODE, FieldType.MULTICHOICE_MAP,
                this.headersMode == null ? new ArrayList<>() : this.headersMode);
        outcome.putDataSetEntry(TaskViewConstants.FIELD_HEADERS_DEFAULT_MODE, FieldType.ENUMERATION_MAP,
                this.headersDefaultMode);
        outcome.putDataSetEntry(TaskViewConstants.FIELD_IS_HEADER_MODE_CHANGEABLE, FieldType.BOOLEAN,
                this.isHeaderModeChangeable);
        outcome.putDataSetEntry(TaskViewConstants.FIELD_ALLOW_HEADER_TABLE_MODE, FieldType.BOOLEAN,
                this.allowHeaderTableMode);
        outcome.putDataSetEntry(TaskViewConstants.FIELD_USE_DEFAULT_HEADERS, FieldType.BOOLEAN,
                this.useDefaultHeaders);
        if (this.defaultHeaders != null) {
            outcome.putDataSetEntry(TaskViewConstants.FIELD_DEFAULT_HEADERS, FieldType.STRING_COLLECTION,
                    this.defaultHeaders);
        }
        outcome.putDataSetEntry(TaskViewConstants.FIELD_SHOW_MORE_MENU, FieldType.BOOLEAN,
                this.showMoreMenu);
        if (this.emptyContentText != null) {
            outcome.putDataSetEntry(TaskViewConstants.FIELD_EMPTY_CONTENT_TEXT, FieldType.I18N,
                    this.emptyContentText);
        }
        outcome.putDataSetEntry(TaskViewConstants.FIELD_EMPTY_CONTENT_ICON, FieldType.TEXT,
                this.emptyContentIcon);

        return outcome;
    }
}
