package com.netgrif.application.engine.configuration;

import com.netgrif.application.engine.workflow.service.FieldActionsCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.endpoint.annotation.DeleteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Endpoint(id = "fieldActions")
public class FieldActionCacheEndpoint {

    private final FieldActionsCacheService cacheService;

    public FieldActionCacheEndpoint(FieldActionsCacheService cacheService) {
        this.cacheService = cacheService;
    }

    @DeleteOperation
    public String clear() {
        cacheService.clearActionCache();
        log.info("actionsCache cleared");
        return "actionsCache cleared";
    }
}
