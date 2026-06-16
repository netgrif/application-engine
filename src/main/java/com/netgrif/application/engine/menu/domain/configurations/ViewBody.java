package com.netgrif.application.engine.menu.domain.configurations;

import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemViewType;
import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.menu.utils.MenuItemUtils;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.annotation.Nullable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class ViewBody {

    @Nullable
    protected FilterBody filterBody;

    public abstract ViewBody getAssociatedViewBody();
    public abstract MenuItemViewType getViewType();
    public abstract String getFilterFieldId();
    public abstract FieldType getFilterType();
    /**
     * Internal method, that must transform data in concrete class and add them into received outcome. Method must return
     * the updated outcome.
     * */
    protected abstract ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome);

    /**
     * Checks if the view has associated view
     *
     * @return true if this view has associated view
     * */
    public boolean hasAssociatedView() {
        return this.getAssociatedViewBody() != null;
    }

    /**
     * @return returns process identifier for this view
     * */
    public String getViewProcessIdentifier() {
        return getViewType().getIdentifier() + "_configuration";
    }

    /**
     * Transforms data of this class into {@link ToDataSetOutcome}, which contains prepared data for the {@link IDataService#setData}
     *
     * @return {@link ToDataSetOutcome} object containing dataSet
     * */
    public ToDataSetOutcome toDataSet() {
        return toDataSet(null);
    };

    /**
     * Transforms data of this class into {@link ToDataSetOutcome}, which contains prepared data for the {@link IDataService#setData}
     *
     * @param associatedViewCase case instance of associated view. If provided, caseRef and taskRef are initialized.
     *
     * @return {@link ToDataSetOutcome} object containing dataSet
     * */
    public ToDataSetOutcome toDataSet(Case associatedViewCase) {
        ToDataSetOutcome outcome = new ToDataSetOutcome();

        if (associatedViewCase != null) {
            outcome.putDataSetEntry(ViewConstants.FIELD_CONFIGURATION_TYPE, FieldType.ENUMERATION_MAP,
                    this.getAssociatedViewBody().getViewType().getIdentifier());
            outcome.putDataSetEntry(ViewConstants.FIELD_VIEW_CONFIGURATION_ID, FieldType.CASE_REF,
                    List.of(associatedViewCase.getStringId()));
            String taskId = MenuItemUtils.findTaskIdInCase(associatedViewCase, ViewConstants.TRANS_SETTINGS_ID);
            outcome.putDataSetEntry(ViewConstants.FIELD_VIEW_CONFIGURATION_FORM, FieldType.TASK_REF, List.of(taskId));
            String allDataTaskId = MenuItemUtils.findTaskIdInCase(associatedViewCase, ViewConstants.TRANS_ALL_MENU_DATA_ID);
            outcome.putDataSetEntry(ViewConstants.FIELD_VIEW_CONFIGURATION_ALL_DATA_FORM, FieldType.TASK_REF, List.of(allDataTaskId));
        }
        if (filterBody != null) {
            filterBody.setType(getFilterType());
            outcome = filterBody.toDataSet(outcome, getFilterFieldId());
        }

        return toDataSetInternal(outcome);
    };
}
