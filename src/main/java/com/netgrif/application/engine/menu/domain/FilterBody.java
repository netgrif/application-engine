package com.netgrif.application.engine.menu.domain;

import com.netgrif.application.engine.petrinet.domain.I18nString;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.startup.DefaultFiltersRunner;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class FilterBody {
    private Case filter;
    private I18nString title;
    private String query;
    private String type;
    private String visibility;

    public FilterBody(Case filterCase) {
        this.filter = filterCase;
    }

    /**
     * Transforms attributes into dataSet for {@link IDataService#setData}
     *
     * @return {@link ToDataSetOutcome} object with dataSet
     * */
    public ToDataSetOutcome toDataSet() {
        ToDataSetOutcome outcome = new ToDataSetOutcome();

        outcome.putDataSetEntry(DefaultFiltersRunner.FILTER_TYPE_FIELD_ID, FieldType.ENUMERATION_MAP, this.type);
        outcome.putDataSetEntry(DefaultFiltersRunner.FILTER_VISIBILITY_FIELD_ID, FieldType.ENUMERATION_MAP, this.visibility);
        outcome.putDataSetEntry(DefaultFiltersRunner.FILTER_I18N_TITLE_FIELD_ID, FieldType.I18N, this.title);
        outcome.getDataSet().put(DefaultFiltersRunner.FILTER_FIELD_ID, Map.of(
                "type", "filter",
                "value", this.query
        ));

        return outcome;
    }
}
