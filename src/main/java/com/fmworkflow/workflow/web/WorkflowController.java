package com.fmworkflow.workflow.web;

import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.dataset.DataSet;
import com.fmworkflow.workflow.service.IWorkflowService;
import com.fmworkflow.workflow.web.requestbodies.CreateCaseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/res/workflow")
public class WorkflowController {

    @Autowired
    private IWorkflowService workflowService;

    @RequestMapping(value = "/case", method = RequestMethod.POST, produces = "application/json")
    public String createCase(@RequestBody CreateCaseBody body) {
        try {
            workflowService.createCase(body.netId, body.title);
            return JsonBuilder.successMessage("Case created successfully");
        } catch (Exception e) { // TODO: 5. 2. 2017 change to custom exception
            e.printStackTrace();
            return JsonBuilder.errorMessage("Failed to create case");
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = "application/json")
    public List<Case> getAll() {
        return workflowService.getAll();
    }

    @RequestMapping(value = "/data/{case}/{transition}", method = RequestMethod.GET)
    public DataSet getDataSet(@PathVariable("case") String caseId, @PathVariable("transition") String transitionId){
        return workflowService.getDataForTransition(caseId, transitionId);
    }

    @RequestMapping(value = "/data/{case}", method = RequestMethod.POST)
    public void modifyData(@PathVariable("case")String caseId, @RequestBody Map<String, String> values){
        workflowService.modifyData(caseId, values);
    }
}