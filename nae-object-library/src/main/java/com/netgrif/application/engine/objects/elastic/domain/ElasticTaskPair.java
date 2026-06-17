package com.netgrif.application.engine.objects.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ElasticTaskPair implements Serializable {

    @Serial
    private static final long serialVersionUID = 8399390623172906801L;

    /**
     * Represents a MongoDB ObjectId in the hex-string form of the task process resource.
     */
    private String task;

    /**
     * Represents an import id of the transition of the task.
     */
    private String transition;

}
