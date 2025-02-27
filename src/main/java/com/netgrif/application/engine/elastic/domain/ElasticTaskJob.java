package com.netgrif.application.engine.elastic.domain;

import com.netgrif.core.elastic.domain.ElasticJob;
import com.netgrif.core.elastic.domain.ElasticTask;
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




