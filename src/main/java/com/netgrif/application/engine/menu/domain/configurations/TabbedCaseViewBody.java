package com.netgrif.application.engine.menu.domain.configurations;

import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.startup.MenuItemViewRegistryRunner;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TabbedCaseViewBody extends ViewBody {
    private String viewSearchType = "fulltext_advanced";
    private String createCaseButtonTitle;
    private String createCaseButtonIcon = "add";
    private boolean requireTitleInCreation = true;
    private boolean showCreateCaseButton = true;
    private String bannedNetsInCreation;
    private boolean showMoreMenu = false;
    private boolean allowHeaderTableMode = true;
    private List<String> headersMode = new ArrayList<>(List.of("sort", "edit", "search"));
    private String headersDefaultMode = "sort";
    private List<String> defaultHeaders;
    private boolean isHeaderModeChangeable = true;
    private boolean useDefaultHeaders = true;

    private ViewBody chainedView;

    @Override
    public ViewBody getAssociatedViewBody() {
        return this.chainedView;
    }

    @Override
    public String getViewIdentifier() {
        return MenuItemViewRegistryRunner.TABBED_CASE_VIEW_ID;
    }

    @Override
    protected ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome) {

        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_VIEW_SEARCH_TYPE, FieldType.ENUMERATION_MAP,
                this.viewSearchType);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_CREATE_CASE_BUTTON_TITLE, FieldType.TEXT,
                this.createCaseButtonTitle);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_CREATE_CASE_BUTTON_ICON, FieldType.TEXT,
                this.createCaseButtonIcon);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_REQUIRE_TITLE_IN_CREATION, FieldType.BOOLEAN,
                this.requireTitleInCreation);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_SHOW_CREATE_CASE_BUTTON, FieldType.BOOLEAN,
                this.showCreateCaseButton);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_BANNED_NETS_IN_CREATION, FieldType.TEXT,
                this.bannedNetsInCreation);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_SHOW_MORE_MENU, FieldType.BOOLEAN,
                this.showMoreMenu);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_ALLOW_HEADER_TABLE_MODE, FieldType.BOOLEAN,
                this.allowHeaderTableMode);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_HEADERS_MODE, FieldType.MULTICHOICE_MAP,
                this.headersMode == null ? new ArrayList<>() : this.headersMode);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_HEADERS_DEFAULT_MODE, FieldType.ENUMERATION_MAP,
                this.headersDefaultMode);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_DEFAULT_HEADERS, FieldType.TEXT,
                this.defaultHeaders != null ? String.join(",", this.defaultHeaders) : null);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_IS_HEADER_MODE_CHANGEABLE, FieldType.BOOLEAN,
                this.isHeaderModeChangeable);
        outcome.putDataSetEntry(TabbedCaseViewConstants.FIELD_USE_CASE_DEFAULT_HEADERS, FieldType.BOOLEAN,
                this.useDefaultHeaders);

        return outcome;
    }
}

