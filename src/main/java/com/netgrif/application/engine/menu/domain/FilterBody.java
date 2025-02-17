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
    private List<String> allowedNets;
    private String icon;
    private String visibility;
    private Map<String, Object> metadata;

    public FilterBody(Case filterCase) {
        this.filter = filterCase;
    }

    /**
     * Gets default metadata with provided filter type
     *
     * @param type type of the filter
     *
     * @return metadata containing filter type as map
     * */
    public static Map<String, Object> getDefaultMetadata(String type) {
        Map<String, Object> resultMap = new HashMap<>();

        resultMap.put("searchCategories", List.of());
        resultMap.put("predicateMetadata", List.of());
        resultMap.put("filterType", type);
        resultMap.put("defaultSearchCategories", true);
        resultMap.put("inheritAllowedNets", false);

        return resultMap;
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
        Map<String, Object> metadata = this.metadata;
        if (metadata == null) {
            metadata = getDefaultMetadata(this.type);
        }
        outcome.getDataSet().put(DefaultFiltersRunner.FILTER_FIELD_ID, Map.of(
                "type", "filter",
                "value", this.query,
                "allowedNets", this.allowedNets,
                "filterMetadata", metadata
        ));

        return outcome;
    }
}
