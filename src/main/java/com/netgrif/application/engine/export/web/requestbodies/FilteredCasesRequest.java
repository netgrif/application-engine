package com.netgrif.application.engine.export.web.requestbodies;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import lombok.Data;

import java.util.List;

@Data
public class FilteredCasesRequest {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<CaseSearchRequest> query;

    private List<String> selectedDataFieldNames;

    private List<String> selectedDataFieldIds;

    private Boolean isIntersection;
}