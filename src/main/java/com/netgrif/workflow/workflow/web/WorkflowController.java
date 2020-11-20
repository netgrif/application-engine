package com.netgrif.workflow.workflow.web;

import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.auth.domain.throwable.UnauthorisedRequestException;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.elastic.domain.ElasticCase;
import com.netgrif.workflow.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.workflow.elastic.web.requestbodies.singleaslist.SingleCaseSearchRequestAsList;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.service.FileFieldInputStream;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowAuthorizationService;
import com.netgrif.workflow.workflow.service.interfaces.IWorkflowService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateCaseBody;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

@RestController()
@RequestMapping("/api/workflow")
@ConditionalOnProperty(
        value = "nae.case.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"Workflow"})
public class WorkflowController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class.getName());

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IElasticCaseService elasticCaseService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IWorkflowAuthorizationService authenticationService;

    @ApiOperation(value = "Create new case", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public CaseResource createCase(@RequestBody CreateCaseBody body, Authentication auth) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            Case useCase = workflowService.createCase(body.netId, body.title, body.color, loggedUser);
            return new CaseResource(useCase);
        } catch (Exception e) { // TODO: 5. 2. 2017 change to custom exception
            log.error("Creating case failed:",e);
            return null;
        }
    }

    @ApiOperation(value = "Get all cases of the system", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<CaseResource> getAll(Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.getAll(pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .getAll(pageable, assembler)).withRel("all");
        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Generic case search with QueryDSL predicate", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/case/search2", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<CaseResource> search2(@QuerydslPredicate(root = Case.class) Predicate predicate, Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.search(predicate, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .search2(predicate, pageable, assembler)).withRel("search2");
        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Generic case search on Elasticsearch database", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/case/search", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<CaseResource> search(@RequestBody SingleCaseSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Pageable pageable, PagedResourcesAssembler<Case> assembler, Authentication auth, Locale locale) {
        LoggedUser user =(LoggedUser) auth.getPrincipal();
        Page<Case> cases = elasticCaseService.search(searchBody.getList(), user, pageable, locale, operation == MergeFilterOperation.AND);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .search(searchBody, operation, pageable, assembler, auth, locale)).withRel("search");

        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, ElasticCase.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Generic case search on Mongo database", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/case/search_mongo", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<CaseResource> searchMongo(@RequestBody Map<String, Object> searchBody, Pageable pageable, PagedResourcesAssembler<Case> assembler, Authentication auth, Locale locale) {
        Page<Case> cases = workflowService.search(searchBody, pageable, (LoggedUser) auth.getPrincipal(), locale);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .searchMongo(searchBody, pageable, assembler, auth, locale)).withRel("search");
        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
        return resources;
    }


    @ApiOperation(value = "Get count of the cases", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/case/count", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CountResponse count(@RequestBody SingleCaseSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Authentication auth, Locale locale) {
        long count = elasticCaseService.count(searchBody.getList(), (LoggedUser) auth.getPrincipal(), locale, operation == MergeFilterOperation.AND);
        return CountResponse.caseCount(count);
    }

    @ApiOperation(value = "Get case by id", authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/case/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public CaseResource getOne(@PathVariable("id") String caseId) {
        Case aCase = workflowService.findOne(caseId);
        if (aCase == null)
            return null;
        return new CaseResource(aCase);
    }

    @ApiOperation(value = "Get all cases by user that created them", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case/author/{id}", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<CaseResource> findAllByAuthor(@PathVariable("id") Long authorId, @RequestBody String petriNet, Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.findAllByAuthor(authorId, petriNet, pageable);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(WorkflowController.class)
                .findAllByAuthor(authorId, petriNet, pageable, assembler)).withRel("author");
        PagedResources<CaseResource> resources = assembler.toResource(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel());
        return resources;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation(value = "Reload tasks of case",
            notes = "Caller must have the ADMIN role",
            authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/case/reload/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource reloadTasks(@PathVariable("id") String caseId) {
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            Case aCase = workflowService.findOne(caseId);
            taskService.reloadTasks(aCase);

            return MessageResource.successMessage("Task reloaded in case ["+caseId+"]");
        } catch (Exception e) {
            log.error("Reloading tasks of case ["+caseId+"] failed:", e);
            return MessageResource.errorMessage("Reloading tasks in case "+caseId+" has failed!");
        }
    }

    @ApiOperation(value = "Delete case", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case/{id}", method = RequestMethod.DELETE, produces = MediaTypes.HAL_JSON_VALUE)
    public MessageResource deleteCase(@PathVariable("id") String caseId, @RequestParam(defaultValue = "false") boolean deleteSubtree) throws UnauthorisedRequestException {
        User logged = userService.getLoggedUser();
        Case requestedCase = workflowService.findOne(caseId);
        if (!this.authenticationService.canCallDelete(logged.transformToLoggedUser(), requestedCase))
            throw new UnauthorisedRequestException("User " + logged.transformToLoggedUser().getUsername() + " doesn't have permission to delete case " + requestedCase.getStringId());

        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            if(deleteSubtree) {
                workflowService.deleteSubtreeRootedAt(caseId);
            } else {
                workflowService.deleteCase(caseId);
            }
            return MessageResource.successMessage("Case " + caseId + " was deleted");
        } catch (UnsupportedEncodingException e) {
            log.error("Deleting case ["+caseId+"] failed:",e);
            return MessageResource.errorMessage("Deleting case " + caseId + " has failed!");
        }
    }

    @ApiOperation(value = "Get all case data", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case/{id}/data", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public DataFieldsResource getAllCaseData(@PathVariable("id") String caseId, Locale locale) {
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            return new DataFieldsResource(workflowService.getData(caseId), locale);
        } catch (UnsupportedEncodingException e) {
            log.error("Getting all case data of ["+caseId+"] failed:", e);
            return new DataFieldsResource(new ArrayList<>(), locale);
        }
    }

    @ApiOperation(value = "Download case file field value", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case/{id}/file/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String caseId, @PathVariable("field") String fieldId) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByCase(caseId, null, fieldId); // TODO resolve no task reference

        if (fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File in field " + fieldId + " within case " + caseId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileFieldInputStream.getFileName());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }

    @ApiOperation(value = "Download one file from cases file list field value", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case/{id}/file/{field}/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFileByName(@PathVariable("id") String caseId, @PathVariable("field") String fieldId, @PathVariable("name") String name) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByCaseAndName(caseId, fieldId, name);

        if (fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File with name " + name + " in field " + fieldId + " within case " + caseId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileFieldInputStream.getFileName());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }
}