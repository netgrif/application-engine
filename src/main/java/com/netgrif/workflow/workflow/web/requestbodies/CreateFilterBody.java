package com.netgrif.workflow.workflow.web.requestbodies;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.netgrif.workflow.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.workflow.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.web.requestbodies.filter.SearchMetadata;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateFilterBody {
    private String title;
//    private int visibility;
    private String description;
    private String type;
    private MergeFilterOperation mergeOperator;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<CaseSearchRequest> caseFilterBodies;
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<ElasticTaskSearchRequest> taskFilterBodies;
    private SearchMetadata searchMetadata;
}
