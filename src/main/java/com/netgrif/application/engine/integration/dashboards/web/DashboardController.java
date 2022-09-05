package com.netgrif.application.engine.integration.dashboards.web;

import com.netgrif.application.engine.integration.dashboards.service.interfaces.IDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard")
public class DashboardController {

    private final IDashboardService dashboardService;

    public DashboardController(IDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Execute Elasticsearch aggregation",
            description = "The provided aggregation is executed and its result is returned",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public String getAggregationByQuery(@RequestBody String query, @RequestParam("type") String type) {
        return dashboardService.searchByQuery(query, type);
    }
}
