package com.netgrif.application.engine.elastic.web.requestbodies;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * Represents the parameters to configure the indexing operation.
 * This class allows customization of batch sizes for cases and tasks,
 * as well as the option to index all data.
 */
@Data
public class IndexParams {

    /**
     * Determines whether to index all available data. Default is {@code false}.
     */
    private boolean indexAll = false;

    /**
     * Specifies the batch size for cases during indexing. Default is {@code 5000}.
     */
    private Integer caseBatchSize = 5000;

    /**
     * Specifies the batch size for tasks during indexing. Default is {@code 20000}.
     */
    private Integer taskBatchSize = 20000;


    /**
     * Specifies the date, from which to consider lastModifiedDate property of cases.
     */
    private LocalDateTime lastRun = null;
}
