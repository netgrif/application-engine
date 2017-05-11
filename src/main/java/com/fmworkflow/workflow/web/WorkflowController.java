package com.fmworkflow.workflow.web;

import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.service.interfaces.IWorkflowService;
import com.fmworkflow.workflow.web.requestbodies.CreateCaseBody;
import com.fmworkflow.workflow.web.responsebodies.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
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
    public PagedResources<CaseResource> getAll(Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.getAll(pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .getAll(pageable,assembler)).withRel("all");
        PagedResources<CaseResource> resources = assembler.toResource(cases,new CaseResourceAssembler(),selfLink);
        ResourceLinkAssembler.addLinks(resources,Case.class,selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/case/search", method = RequestMethod.POST)
    public PagedResources<CaseResource> searchCases(@RequestBody List<String> petriNets, Pageable pageable, PagedResourcesAssembler<Case> assembler){
        Page<Case> cases = workflowService.searchCase(petriNets,pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .searchCases(petriNets,pageable,assembler)).withRel("search");
        PagedResources<CaseResource> resources = assembler.toResource(cases,new CaseResourceAssembler(),selfLink);
        ResourceLinkAssembler.addLinks(resources,Case.class,selfLink.getRel());
        return resources;
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