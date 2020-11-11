package com.netgrif.workflow.integration.dashboards.web;

import com.netgrif.workflow.integration.dashboards.service.interfaces.IDashboardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Authorization;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/dashboard")
@Api(tags = {"Dashboard"}, authorizations = @Authorization("BasicAuth"))
public class DashboardController {

    @Autowired
    private IDashboardService dashboardService;

    @ApiOperation(value = "Execute Elasticsearch aggregation",
            notes = "The provided aggregation is executed and its result is returned",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public String getAggregationByQuery(@RequestBody String query, @RequestParam("type") String type){
        return dashboardService.searchByQuery(query, type);
    }
}
