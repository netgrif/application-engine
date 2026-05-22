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
    private List<String> allowedNets;
    private String icon;
    private String visibility;
    private Map<String, Object> metadata;

    /**
     * Gets default metadata with provided filter type
     *
     * @param type type of the filter
     *
     * @return metadata containing filter type as map
     * */
    public static Map<String, Object> getDefaultMetadata(String type, boolean allAllowedNets) {
        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("searchCategories", List.of());
        resultMap.put("predicateMetadata", List.of());
        resultMap.put("filterType", type);
        resultMap.put("defaultSearchCategories", true);
        resultMap.put("inheritAllowedNets", false);
        resultMap.put("allAllowedNets", allAllowedNets);

        return resultMap;
    }

    /**
     * Transforms attributes into dataSet for {@link IDataService#setData}
     *
     * @return {@link ToDataSetOutcome} object with dataSet
     * */
    public ToDataSetOutcome toDataSet(ToDataSetOutcome viewDataSetOutcome, String filterFieldId) {
        Map<String, Object> metadata = this.metadata;
        if (metadata == null) {
            metadata = getDefaultMetadata(this.type, this.allowedNets == null);
        }
        Map<String, Object> dataSetValues = new HashMap<>();
        dataSetValues.put("type", "filter");
        dataSetValues.put("value", this.query);
        dataSetValues.put("filterMetadata", metadata);
        dataSetValues.put("allowedNets", this.allowedNets);
        viewDataSetOutcome.getDataSet().put(filterFieldId, dataSetValues);

        return viewDataSetOutcome;
    }
}
