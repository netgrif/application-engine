package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.domain.ElasticCase;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleCaseSearchRequestAsList;
import com.netgrif.application.engine.eventoutcomes.LocalisedEventOutcomeFactory;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessage;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessageResource;
import com.netgrif.application.engine.workflow.service.FileFieldInputStream;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.requestbodies.CreateCaseBody;
import com.netgrif.application.engine.workflow.web.responsebodies.*;
import com.querydsl.core.types.Predicate;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.querydsl.binding.QuerydslPredicate;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
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

    private final IWorkflowService workflowService;
    private final ITaskService taskService;
    private final IElasticCaseService elasticCaseService;
    private final IDataService dataService;

    public WorkflowController(IWorkflowService workflowService, ITaskService taskService, IElasticCaseService elasticCaseService, IDataService dataService) {
        this.workflowService = workflowService;
        this.taskService = taskService;
        this.elasticCaseService = elasticCaseService;
        this.dataService = dataService;
    }


    @PreAuthorize("@workflowAuthorizationService.canCallCreate(#auth.getPrincipal(), #body.netId)")
    @ApiOperation(value = "Create new case", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/case", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<EventOutcomeWithMessage> createCase(@RequestBody CreateCaseBody body, Authentication auth, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            CreateCaseEventOutcome outcome = workflowService.createCase(body.netId, body.title, body.color, loggedUser, locale);
            return EventOutcomeWithMessageResource.successMessage("Case with id " + outcome.getCase().getStringId() + " was created succesfully",
                    LocalisedEventOutcomeFactory.from(outcome,locale));
        } catch (Exception e) { // TODO: 5. 2. 2017 change to custom exception
            log.error("Creating case failed:",e);
            return EventOutcomeWithMessageResource.errorMessage("Creating case failed" + e.getMessage());
        }
    }

    @ApiOperation(value = "Get all cases of the system", authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/all", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<CaseResource> getAll(Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.getAll(pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                .getAll(pageable, assembler)).withRel("all");
        PagedModel<CaseResource> resources = assembler.toModel(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel().toString());
        return resources;
    }

    // TODO:  NAE-1645 search by ObjectId[] for tree-case.service {stringId: (childCaseRef.value as Array<string>)}
    @ApiOperation(value = "Generic case search with QueryDSL predicate", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/case/search2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<CaseResource> search2(@QuerydslPredicate(root = Case.class) Predicate predicate, Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.search(predicate, pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                .search2(predicate, pageable, assembler)).withRel("search2");
        PagedModel<CaseResource> resources = assembler.toModel(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel().toString());
        return resources;
    }

    @ApiOperation(value = "Generic case search on Elasticsearch database", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/case/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<CaseResource> search(@RequestBody SingleCaseSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Pageable pageable, PagedResourcesAssembler<Case> assembler, Authentication auth, Locale locale) {
        LoggedUser user = (LoggedUser) auth.getPrincipal();
        Page<Case> cases = elasticCaseService.search(searchBody.getList(), user, pageable, locale, operation == MergeFilterOperation.AND);

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                .search(searchBody, operation, pageable, assembler, auth, locale)).withRel("search");

        PagedModel<CaseResource> resources = assembler.toModel(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, ElasticCase.class, selfLink.getRel().toString());
        return resources;
    }

    @ApiOperation(value = "Get count of the cases", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/case/count", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
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
    public PagedModel<CaseResource> findAllByAuthor(@PathVariable("id") String authorId, @RequestBody String petriNet, Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.findAllByAuthor(authorId, petriNet, pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                .findAllByAuthor(authorId, petriNet, pageable, assembler)).withRel("author");
        PagedModel<CaseResource> resources = assembler.toModel(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel().toString());
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

            return MessageResource.successMessage("Task reloaded in case [" + caseId + "]");
        } catch (Exception e) {
            log.error("Reloading tasks of case [" + caseId + "] failed:", e);
            return MessageResource.errorMessage("Reloading tasks in case " + caseId + " has failed!");
        }
    }

    @PreAuthorize("@workflowAuthorizationService.canCallDelete(#auth.getPrincipal(), #caseId)")
    @ApiOperation(value = "Delete case", authorizations = @Authorization("BasicAuth"))
    @DeleteMapping(value = "/case/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<EventOutcomeWithMessage>  deleteCase(Authentication auth, @PathVariable("id") String caseId, @RequestParam(defaultValue = "false") boolean deleteSubtree) {
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8.name());
            DeleteCaseEventOutcome outcome;
            if(deleteSubtree) {
                outcome = workflowService.deleteSubtreeRootedAt(caseId);
            } else {
                outcome = workflowService.deleteCase(caseId);
            }
            return EventOutcomeWithMessageResource.successMessage("Case " + caseId + " was deleted",
                    LocalisedEventOutcomeFactory.from(outcome,LocaleContextHolder.getLocale()));
        } catch (UnsupportedEncodingException e) {
            log.error("Deleting case [" + caseId + "] failed:", e);
            return EventOutcomeWithMessageResource.errorMessage("Deleting case " + caseId + " has failed!");
        }
    }

    @ApiOperation(value = "Download case file field value", authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/case/{id}/file/{field}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String caseId, @PathVariable("field") String fieldId) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByCase(caseId, null, fieldId, false);

        if (fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File in field " + fieldId + " within case " + caseId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileFieldInputStream.getFileName() + "\"");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }

    @ApiOperation(value = "Download one file from cases file list field value", authorizations = @Authorization("BasicAuth"))
    @GetMapping(value = "/case/{id}/file/{field}/{name}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFileByName(@PathVariable("id") String caseId, @PathVariable("field") String fieldId, @PathVariable("name") String name) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByCaseAndName(caseId, fieldId, name);

        if (fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File with name " + name + " in field " + fieldId + " within case " + caseId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileFieldInputStream.getFileName() + "\"");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }
}