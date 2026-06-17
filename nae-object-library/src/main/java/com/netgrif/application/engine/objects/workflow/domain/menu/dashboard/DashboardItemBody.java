package com.netgrif.application.engine.objects.workflow.domain.menu.dashboard;

import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.objects.workflow.domain.menu.ToDataSetOutcome;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DashboardItemBody {

    private String id;
    private String menuItems;
    private boolean isInternal;
    private String externalUrl;
    private String externalIcon;
    private String iconColor;
    private String itemIcon;
    private String fontColor;
    private String fontWeight;
    private I18nString name;
    private boolean inheritIcon;
    private boolean inheritName;
    private boolean isActive;

    public DashboardItemBody(String id, String menuItems, String itemIcon, I18nString name, boolean isInternal) {
        this.id = id;
        this.menuItems = menuItems;
        this.isInternal = isInternal;
        this.itemIcon = itemIcon;
        this.name = name;
    }

    public ToDataSetOutcome toDataSet() {
        ToDataSetOutcome outcome = new ToDataSetOutcome();
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_ID, FieldType.TEXT, this.id);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_MENU_ITEM_LIST, FieldType.ENUMERATION_MAP, this.menuItems);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_IS_INTERNAL, FieldType.BOOLEAN, this.isInternal);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_EXTERNAL_URL, FieldType.TEXT, this.externalUrl);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_EXTERNAL_ICON, FieldType.BOOLEAN, this.externalIcon);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_ICON_COLOR, FieldType.TEXT, this.iconColor);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_ITEM_ICON, FieldType.TEXT, this.itemIcon);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_FONT_COLOR, FieldType.TEXT, this.fontColor);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_FONT_WEIGHT, FieldType.TEXT, this.fontWeight);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_ITEM_NAME, FieldType.I18N, this.name);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_INHERIT_ICON, FieldType.BOOLEAN, this.inheritIcon);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_INHERIT_NAME, FieldType.BOOLEAN, this.inheritName);
        outcome.putDataSetEntry(DashboardItemConstants.FIELD_IS_ACTIVE, FieldType.BOOLEAN, this.isActive);
        return outcome;
    }
}
