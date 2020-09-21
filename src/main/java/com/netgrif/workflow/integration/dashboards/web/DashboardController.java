package com.netgrif.workflow.integration.dashboards.web;

import com.netgrif.workflow.integration.dashboards.service.interfaces.IDashboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private IDashboardService dashboardService;

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    @ResponseBody
    public String getAggregationByQuery(@RequestBody String query, @RequestParam("type") String type){
        return dashboardService.searchByQuery(query, type);
    }
}
