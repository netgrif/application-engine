package com.netgrif.application.engine.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElasticTaskJob {

    private ElasticJob jobType;

    private ElasticTask task;

    public String getTaskId() {
        return getTask().getTaskId();
    }
}




