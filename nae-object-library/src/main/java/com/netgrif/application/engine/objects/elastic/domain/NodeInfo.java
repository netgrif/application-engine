package com.netgrif.application.engine.objects.elastic.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NodeInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 7454569786379865L;

    private String nodeHost;
    private int nodePort;
}
