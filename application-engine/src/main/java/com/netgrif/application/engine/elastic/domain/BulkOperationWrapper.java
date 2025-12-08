package com.netgrif.application.engine.elastic.domain;


import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.util.EventObject;

@Data
@Builder
@AllArgsConstructor
public class BulkOperationWrapper {

    private BulkOperation operation;

    private EventObject publishableEvent;
}
