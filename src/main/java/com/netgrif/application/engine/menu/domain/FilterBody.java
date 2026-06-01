package com.netgrif.application.engine.menu.domain;

import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
public class FilterBody {
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
    public ToDataSetOutcome toDataSet(ToDataSetOutcome viewDataSetOutcome, String filterFieldId) {
        // todo 23 type
        Map<String, Object> dataSetValues = new HashMap<>();
        dataSetValues.put("type", "filter");
        dataSetValues.put("value", this.query);
        viewDataSetOutcome.getDataSet().put(filterFieldId, dataSetValues);

        return viewDataSetOutcome;
    }
}
