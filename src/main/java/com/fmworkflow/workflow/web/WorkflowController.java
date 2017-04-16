package com.fmworkflow.workflow.web;

import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.service.interfaces.IWorkflowService;
import com.fmworkflow.workflow.web.requestbodies.CreateCaseBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/res/workflow")
public class WorkflowController {

    @Autowired
    private IWorkflowService workflowService;

    @RequestMapping(value = "/case", method = RequestMethod.POST)
    public String createCase(@RequestBody CreateCaseBody body) {
        try {
            workflowService.createCase(body.netId, body.title, body.color);
            return JsonBuilder.successMessage("Case created successfully");
        } catch (Exception e) { // TODO: 5. 2. 2017 change to custom exception
            e.printStackTrace();
            return JsonBuilder.errorMessage("Failed to create case");
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Case> getAll() {
        return workflowService.getAll();
    }

//    @RequestMapping(value = "/data/{case}/{transition}", method = RequestMethod.GET)
//    public DataSet getDataSet(@PathVariable("case") String caseId, @PathVariable("transition") String transitionId){
//        return workflowService.getDataForTransition(caseId, transitionId);
//    }
//
//    @RequestMapping(value = "/data/{case}", method = RequestMethod.POST)
//    public void modifyData(@PathVariable("case")String caseId, @RequestBody Map<String, String> values){
//        workflowService.modifyData(caseId, values);
//    }
}