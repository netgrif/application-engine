package com.netgrif.application.engine.menu.domain.configurations;

import com.netgrif.application.engine.menu.domain.FilterBody;
import com.netgrif.application.engine.menu.domain.MenuItemView;
import com.netgrif.application.engine.menu.domain.ToDataSetOutcome;
import com.netgrif.application.engine.menu.utils.MenuItemUtils;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.workflow.domain.Case;
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
    public abstract MenuItemView getViewType();
    /**
     * todo javadoc
     * */
    protected abstract ToDataSetOutcome toDataSetInternal(ToDataSetOutcome outcome);

    /**
     * todo javadoc
     * */
    public boolean hasAssociatedView() {
        return this.getAssociatedViewBody() != null;
    }

    /**
     * todo javadoc
     * */
    public String getViewProcessIdentifier() {
        return getViewType().getIdentifier() + "_configuration";
    }

    /**
     * todo javadoc
     * */
    public ToDataSetOutcome toDataSet() {
        return toDataSet(null, null);
    };

    /**
     * todo javadoc
     * */
    public ToDataSetOutcome toDataSet(Case associatedViewCase, Case filterCase) {
        ToDataSetOutcome outcome = new ToDataSetOutcome();

        if (associatedViewCase != null) {
            outcome.putDataSetEntry(ViewConstants.FIELD_VIEW_CONFIGURATION_ID, FieldType.CASE_REF,
                    List.of(associatedViewCase.getStringId()));
            String taskId = MenuItemUtils.findTaskIdInCase(associatedViewCase, ViewConstants.TRANS_SETTINGS_ID);
            outcome.putDataSetEntry(ViewConstants.FIELD_VIEW_CONFIGURATION_FORM, FieldType.TASK_REF, List.of(taskId));
        }
        if (filterCase != null) {
            outcome.putDataSetEntry(ViewConstants.FIELD_VIEW_FILTER_CASE, FieldType.CASE_REF, List.of(filterCase.getStringId()));
        }

        return toDataSetInternal(outcome);
    };
}
