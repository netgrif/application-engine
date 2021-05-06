package com.netgrif.workflow.elastic.web.requestbodies;

import com.netgrif.workflow.workflow.web.requestbodies.TaskSearchRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ElasticTaskSearchRequest extends TaskSearchRequest {
    public String query;
}
