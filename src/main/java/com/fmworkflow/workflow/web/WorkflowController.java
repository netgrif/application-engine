package com.fmworkflow.workflow.web;

import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.service.interfaces.IWorkflowService;
import com.fmworkflow.workflow.web.requestbodies.CreateCaseBody;
import com.fmworkflow.workflow.web.responsebodies.CaseResource;
import com.fmworkflow.workflow.web.responsebodies.CasesResource;
import com.fmworkflow.workflow.web.responsebodies.MessageResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/res/workflow")
public class WorkflowController {

    @Autowired
    private IWorkflowService workflowService;

    @RequestMapping(value = "/case", method = RequestMethod.POST)
    public MessageResource createCase(@RequestBody CreateCaseBody body) {
        try {
            workflowService.createCase(body.netId, body.title, body.color);
            return MessageResource.successMessage("Case created successfully");
        } catch (Exception e) { // TODO: 5. 2. 2017 change to custom exception
            e.printStackTrace();
            return MessageResource.errorMessage("Failed to create case");
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Case> getAll() {
        return workflowService.getAll();
    }

    @RequestMapping(value = "/case/search", method = RequestMethod.POST)
    public CasesResource searchCases(@RequestBody List<String> petriNets){
        List<CaseResource> resources = new ArrayList<>();
        workflowService.searchCase(petriNets).forEach(useCase -> resources.add(new CaseResource(useCase)));
        return new CasesResource(resources,"search");
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