package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateCaseBody;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/res/workflow")
public class WorkflowController {

    @Autowired
    private IWorkflowService workflowService;

    @RequestMapping(value = "/case", method = RequestMethod.POST)
    public CaseResource createCase(@RequestBody CreateCaseBody body, Authentication auth) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            Case useCase = workflowService.createCase(body.netId, body.title, body.color, loggedUser.getId());
            return new CaseResource(useCase);
        } catch (Exception e) { // TODO: 5. 2. 2017 change to custom exception
            e.printStackTrace();
            return null;
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public PagedResources<CaseResource> getAll(Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.getAll(pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .getAll(pageable, assembler)).withRel("all");
        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/case/search", method = RequestMethod.POST)
    public PagedResources<CaseResource> searchCases(@RequestBody List<String> petriNets, Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.searchCase(petriNets, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .searchCases(petriNets, pageable, assembler)).withRel("search");
        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/case/author/{id}", method = RequestMethod.POST)
    public PagedResources<CaseResource> findAllByAuthor(@PathVariable("id") Long authorId, @RequestBody String petriNet, Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.findAllByAuthor(authorId, petriNet, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .findAllByAuthor(authorId, petriNet, pageable, assembler)).withRel("author");
        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/case/{id}", method = RequestMethod.DELETE)
    public MessageResource deleteCase(@PathVariable("id") String caseId){
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            workflowService.deleteCase(caseId);
            return MessageResource.successMessage("Case "+caseId+" was deleted");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return MessageResource.errorMessage("Deleting case "+caseId+" has failed!");
        }
    }

    @RequestMapping(value= "/case/{id}/data", method = RequestMethod.GET)
    public DataFieldsResource getAllCaseData(@PathVariable("id") String caseId){
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            return new DataFieldsResource(workflowService.getData(caseId),null);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new DataFieldsResource(new ArrayList<>(),null);
        }
    }
}