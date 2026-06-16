package com.netgrif.application.engine.menu.domain;

import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class FilterBody {
    private String query;
    private FieldType type;

    /**
     * Transforms attributes into dataSet for {@link IDataService#setData}
     *
     * @return {@link ToDataSetOutcome} object with dataSet
     * */
    public ToDataSetOutcome toDataSet(ToDataSetOutcome viewDataSetOutcome, String filterFieldId) {
        Map<String, Object> dataSetValues = new HashMap<>();
        if (this.type == null) {
            throw new IllegalArgumentException("Filter type is not provided");
        }
        dataSetValues.put("type", this.type.getName());
        dataSetValues.put("value", this.query);
        viewDataSetOutcome.getDataSet().put(filterFieldId, dataSetValues);

        return viewDataSetOutcome;
    }
}
