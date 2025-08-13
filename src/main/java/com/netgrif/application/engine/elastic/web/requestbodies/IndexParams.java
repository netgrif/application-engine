package com.netgrif.application.engine.elastic.web.requestbodies;

import lombok.Data;

@Data
public class IndexParams {
    private boolean indexAll = false;
    private Integer caseBatchSize = 5000;
    private Integer taskBatchSize = 20000;
}
