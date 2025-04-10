package com.netgrif.application.engine.menu.domain.dashboard;

import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@AllArgsConstructor
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
     * menuItemId
     */
    private String menuItemId;
    /**
     * should dashboard toolbar contains menu with options.
     */
    private boolean simpleDashboard;
    /**
     * should dashboard toolbar menu with profile.
     */
    private boolean profileDashboard;
    /**
     * should dashboard toolbar contains menu with language selection
     */
    private boolean languageDashboard;
    /**
     * should dashboard toolbar contains logout button.
     */
    private boolean logoutDashboard;


    public DashboardManagementBody(String id, I18nString name) {
        this.id = id;
        this.name = name;
    }

    public ToDataSetOutcome toDataSet() {
        DashboardToDataSetOutcome outcome = new DashboardToDataSetOutcome();
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_ID, FieldType.TEXT, this.id);
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_NAME, FieldType.I18N, this.name);
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_LOGO, FieldType.TEXT, this.logo);
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_SIMPLE_DASHBOARD, FieldType.BOOLEAN, this.simpleDashboard);
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_PROFILE_DASHBOARD, FieldType.BOOLEAN, this.profileDashboard);
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_LANGUAGE_DASHBOARD, FieldType.BOOLEAN, this.profileDashboard);
        outcome.putDataSetEntry(DashboardManagementConstants.FIELD_LOGOUT_DASHBOARD, FieldType.BOOLEAN, this.logoutDashboard);
        if (menuItemId != null) {
            outcome.putDataSetEntryWithOptions(DashboardManagementConstants.FIELD_EXISTING_MENU_ITEMS, FieldType.ENUMERATION_MAP, Map.of(this.menuItemId, new I18nString("")), this.menuItemId);
            outcome.putDataSetEntry(DashboardManagementConstants.FIELD_ADD_NEW_ITEM, FieldType.BUTTON, 1);
        }
        return outcome;
    }
}
