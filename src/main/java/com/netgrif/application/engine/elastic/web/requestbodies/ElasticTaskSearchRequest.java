package com.netgrif.application.engine.elastic.web.requestbodies;

import com.netgrif.application.engine.workflow.web.requestbodies.TaskSearchRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
public class ElasticTaskSearchRequest extends TaskSearchRequest {
    public String query;
}
