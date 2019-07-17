package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.service.IElasticCaseService;
import com.netgrif.workflow.elastic.web.CaseSearchRequest;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.service.FileFieldInputStream;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateCaseBody;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import com.querydsl.core.types.Predicate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

@RestController()
@RequestMapping("/api/workflow")
public class WorkflowController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class.getName());

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Autowired
    private IDataService dataService;

    @RequestMapping(value = "/case", method = RequestMethod.POST)
    public CaseResource createCase(@RequestBody CreateCaseBody body, Authentication auth) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            Case useCase = workflowService.createCase(body.netId, body.title, body.color, loggedUser);
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

    @PostMapping("/case/search2")
    public PagedResources<CaseResource> search2(@QuerydslPredicate(root = Case.class) Predicate predicate, Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.search(predicate, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .search2(predicate, pageable, assembler)).withRel("search2");
        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
        return resources;
    }

    @PostMapping(value = "/case/search", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<CaseResource> search(@RequestBody CaseSearchRequest searchBody, Pageable pageable, PagedResourcesAssembler<Case> assembler, Authentication auth, Locale locale) {
        LoggedUser user =(LoggedUser) auth.getPrincipal();
        long start = System.currentTimeMillis();
        Page<Case> cases = elasticCaseService.search(searchBody, user, pageable);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .search(searchBody, pageable, assembler, auth, locale)).withRel("search");

        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, ElasticCase.class, selfLink.getRel());
        return resources;
    }

    @PostMapping(value = "/case/count", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CountResponse count(@RequestBody CaseSearchRequest query, Authentication auth) {
        long count = elasticCaseService.count(query, (LoggedUser) auth.getPrincipal());
        return CountResponse.caseCount(count);
    }

//    @GetMapping(value = "/case/fulltext", produces = MediaTypes.HAL_JSON_VALUE)
//    public PagedResources<CaseResource> fullTextSearch(@RequestParam("process") String process, @RequestParam("search") String searchInput, Pageable pageable, PagedResourcesAssembler<Case> assembler, Authentication auth, Locale locale) {
//        Page<Case> cases = workflowService.fullTextSearch(process, searchInput, pageable);
//        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
//                .fullTextSearch(process, searchInput, pageable, assembler, auth, locale)).withRel("fullTextSearch");
//        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
//        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
//        return resources;
//    }

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
    public MessageResource deleteCase(@PathVariable("id") String caseId) {
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            workflowService.deleteCase(caseId);
            return MessageResource.successMessage("Case " + caseId + " was deleted");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return MessageResource.errorMessage("Deleting case " + caseId + " has failed!");
        }
    }

    @RequestMapping(value = "/case/{id}/data", method = RequestMethod.GET)
    public DataFieldsResource getAllCaseData(@PathVariable("id") String caseId, Locale locale) {
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            return new DataFieldsResource(workflowService.getData(caseId), locale);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new DataFieldsResource(new ArrayList<>(), locale);
        }
    }

    @RequestMapping(value = "/case/{caseId}/field/{fieldId}", method = RequestMethod.GET)
    public List<Case> getCaseFieldChoices(@PathVariable("caseId") String caseId, @PathVariable("fieldId") String fieldId, Pageable pageable) {
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            fieldId = URLDecoder.decode(fieldId, StandardCharsets.UTF_8.name());
            return workflowService.getCaseFieldChoices(pageable, caseId, fieldId);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return new LinkedList<>();
        }
    }

    @RequestMapping(value = "/case/{id}/file/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String caseId, @PathVariable("field") String fieldId) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByCase(caseId, fieldId);

        if (fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File in field " + fieldId + " within case " + caseId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileFieldInputStream.getFileName());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));


//        FileSystemResource fileResource = dataService.getFileByCase(caseId, fieldId);
//        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
//        response.setHeader("Content-Disposition", "attachment; filename=" + fileResource.getFilename().substring(fileResource.getFilename().indexOf('-', fileResource.getFilename().indexOf('-') + 1) + 1));
//        return fileResource;
    }
}