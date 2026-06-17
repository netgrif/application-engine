package com.netgrif.application.engine.objects.workflow.domain.menu.dashboard;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.objects.workflow.domain.menu.ToDataSetOutcome;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class DashboardManagementBody {
    /**
     * id
     */
    private String id;

    /**
     * name with translation
     */
    private I18nString name;
    /**
     * logo from assets on frontend
     */
    private String logo;
    /**
     * dashboardItems
     */
    private HashMap<String, I18nString> dashboardItems;
    /**
     * mapping for menuItems to DashboardItems
     */
    private HashMap<String, I18nString> menuItemsToDashboardItems;
    /**
     * should dashboard toolbar contains menu with options.
     */
    @Builder.Default
    private boolean simpleDashboard = false;
    /**
     * should dashboard toolbar menu with profile.
     */
    @Builder.Default
    private boolean profileDashboard = false;
    /**
     * should dashboard toolbar contains menu with language selection
     */
    @Builder.Default
    private boolean languageDashboard = false;
    /**
     * should dashboard toolbar contains logout button.
     */
    @Builder.Default
    private boolean logoutDashboard = false;


    public DashboardManagementBody(String id, I18nString name) {
        this.id = id;
        this.name = name;
    }

    public ToDataSetOutcome toDataSet() {
        DashboardToDataSetOutcome outcome = new DashboardToDataSetOutcome();
        if (this.id != null) {
            outcome.putDataSetEntry(DashboardManagementConstants.FIELD_ID, FieldType.TEXT, this.id);
        }
        if (this.id != null) {
            outcome.putDataSetEntry(DashboardManagementConstants.FIELD_NAME, FieldType.I18N, this.name);
        }
        if (this.logo != null) {
            outcome.putDataSetEntry(DashboardManagementConstants.FIELD_LOGO, FieldType.TEXT, this.logo);
        }
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_SIMPLE_DASHBOARD, FieldType.BOOLEAN, this.simpleDashboard);
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_PROFILE_DASHBOARD, FieldType.BOOLEAN, this.profileDashboard);
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_LANGUAGE_DASHBOARD, FieldType.BOOLEAN, this.profileDashboard);
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_LOGOUT_DASHBOARD, FieldType.BOOLEAN, this.logoutDashboard);
        if (this.dashboardItems != null) {
            outcome.putDataSetEntryWithOptions(DashboardManagementConstants.FIELD_DASHBOARD_ITEM_LIST, FieldType.ENUMERATION_MAP, this.dashboardItems, null);
            outcome.putDataSetEntryWithOptions(DashboardManagementConstants.FIELD_DASHBOARD_ITEM_TO_MENU_ITEM_LIST, FieldType.ENUMERATION_MAP, this.menuItemsToDashboardItems, null);
            outcome.putDataSetEntry(DashboardManagementConstants.FIELD_ITEMS_ORDER, FieldType.TEXT, String.join(",", this.dashboardItems.keySet()));
        }
        return outcome;
    }
}
