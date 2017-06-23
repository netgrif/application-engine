package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateCaseBody;
import com.netgrif.workflow.workflow.web.responsebodies.CaseResource;
import com.netgrif.workflow.workflow.web.responsebodies.CaseResourceAssembler;
import com.netgrif.workflow.workflow.web.responsebodies.ResourceLinkAssembler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

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

    @RequestMapping(value = "/case/author/{id}", method = RequestMethod.GET)
    public PagedResources<CaseResource> findAllByAuthor(@PathVariable("id") Long authorId, Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.findAllByAuthor(authorId, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .findAllByAuthor(authorId, pageable, assembler)).withRel("author/" + authorId);
        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
        return resources;
    }
}