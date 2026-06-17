package com.netgrif.application.engine.objects.petrinet.domain.dataset;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class MinIoStorage extends Storage {

    private String bucket;

    public MinIoStorage() {
        super("minio");
    }
}
