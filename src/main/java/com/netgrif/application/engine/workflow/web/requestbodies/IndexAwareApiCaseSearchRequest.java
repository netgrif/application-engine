package com.netgrif.application.engine.workflow.web.requestbodies;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IndexAwareApiCaseSearchRequest {

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    protected List<String> menuItemIds;

    protected Boolean searchAll = false;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    protected List<CaseSearchRequest> body;
}
