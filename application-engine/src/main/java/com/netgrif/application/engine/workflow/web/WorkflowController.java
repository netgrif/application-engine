package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleCaseSearchRequestAsList;
import com.netgrif.application.engine.eventoutcomes.LocalisedEventOutcomeFactory;
import com.netgrif.application.engine.objects.elastic.domain.ElasticCase;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessage;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessageResource;
import com.netgrif.application.engine.workflow.service.FileFieldInputStream;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.requestbodies.CreateCaseBody;
import com.netgrif.application.engine.workflow.web.responsebodies.*;
import com.querydsl.core.types.Predicate;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
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
import java.util.Locale;
import java.util.Map;

@RestController()
@RequestMapping("/api/workflow")
@ConditionalOnProperty(
        value = "netgrif.engine.web.case-enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Workflow")
@RequiredArgsConstructor
public class WorkflowController {

    private static final Logger log = LoggerFactory.getLogger(WorkflowController.class.getName());

    private final IWorkflowService workflowService;

    private final ITaskService taskService;

    private final IElasticCaseService elasticCaseService;

    private final IDataService dataService;

    private final UserService userService;


    @PreAuthorize("@workflowAuthorizationService.canCallCreate(@userService.getLoggedUser(), #body.netId)")
    @Operation(summary = "Create new case", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/case", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<EventOutcomeWithMessage> createCase(@RequestBody CreateCaseBody body, Locale locale) {
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(userService.getLoggedUser());
        try {
            CreateCaseEventOutcome outcome = workflowService.createCase(body.netId, body.title, body.color, loggedUser, locale);
            return EventOutcomeWithMessageResource.successMessage("Case with id " + outcome.getCase().getStringId() + " was created succesfully",
                    LocalisedEventOutcomeFactory.from(outcome, locale));
        } catch (Exception e) {
            log.error("Creating case failed:", e);
            return EventOutcomeWithMessageResource.errorMessage("Creating case failed" + e.getMessage());
        }
    }

    @Operation(summary = "Get all cases of the system, paginated", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/all", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<CaseResource> getAll(Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.getAll(pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                .getAll(pageable, assembler)).withRel("all");
        return getCaseResources(pageable, assembler, cases, selfLink);
    }

    @Operation(summary = "Generic case search with QueryDSL predicate, paginated", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/case/search2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<CaseResource> search2(@QuerydslPredicate(root = Case.class) Predicate predicate, Pageable pageable, PagedResourcesAssembler<Case> assembler) {
        Page<Case> cases = workflowService.search(predicate, pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                .search2(predicate, pageable, assembler)).withRel("search2");
        return getCaseResources(pageable, assembler, cases, selfLink);
    }

    @Operation(summary = "Generic case search on Elasticsearch database, paginated", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/case/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<CaseResource> search(@RequestBody SingleCaseSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Pageable pageable, PagedResourcesAssembler<Case> assembler, Locale locale) {
        LoggedUser user = ActorTransformer.toLoggedUser(userService.getLoggedUser());

        Page<Case> cases = elasticCaseService.search(searchBody.getList(), user, pageable, locale, operation == MergeFilterOperation.AND);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                .search(searchBody, operation, pageable, assembler, locale)).withRel("search");

        PagedModel<CaseResource> resources = assembler.toModel(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, ElasticCase.class, selfLink.getRel().toString());
        return PagedModel.of(cases.stream().map(CaseResource::new).toList(), new PagedModel.PageMetadata(pageable.getPageSize(), pageable.getPageNumber(), cases.getTotalElements()));
    }

    @Operation(summary = "Generic case search on Mongo database, paginated", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/case/search_mongo", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<CaseResource> searchMongo(@RequestBody Map<String, Object> searchBody, Pageable pageable, PagedResourcesAssembler<Case> assembler, Locale locale) {
        Page<Case> cases = workflowService.search(searchBody, pageable, ActorTransformer.toLoggedUser(userService.getLoggedUser()), locale);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                .searchMongo(searchBody, pageable, assembler, locale)).withRel("search");
        return getCaseResources(pageable, assembler, cases, selfLink);
    }


    @Operation(summary = "Get count of the cases", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/case/count", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CountResponse count(@RequestBody SingleCaseSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Locale locale) {
        long count = elasticCaseService.count(searchBody.getList(), ActorTransformer.toLoggedUser(userService.getLoggedUser()), locale, operation == MergeFilterOperation.AND);
        return CountResponse.caseCount(count);
    }

    @Operation(summary = "Get case by id", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/case/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public CaseResource getOne(@PathVariable("id") String caseId) {
        Case aCase = workflowService.findOne(caseId);
        if (aCase == null)
            return null;
        return new CaseResource(aCase);
    }

    @Operation(summary = "Get all cases by user that created them, paginated", security = {@SecurityRequirement(name = "BasicAuth")})
    @RequestMapping(value = "/case/author/{id}", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<CaseResource> findAllByAuthor(@PathVariable("id") String authorId, @RequestBody String petriNet, PagedResourcesAssembler<Case> assembler, Pageable pageable) {
        Page<Case> cases = workflowService.findAllByAuthor(authorId, petriNet, pageable);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(WorkflowController.class)
                .findAllByAuthor(authorId, petriNet, assembler, pageable)).withRel("author");
        return getCaseResources(pageable, assembler, cases, selfLink);
    }

    @PreAuthorize("@authorizationService.hasAuthority('ADMIN')")
    @Operation(summary = "Reload tasks of case",
            description = "Caller must have the ADMIN role",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/case/reload/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource reloadTasks(@PathVariable("id") String caseId) {
        try {
            caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8);
            Case aCase = workflowService.findOne(caseId);
            taskService.reloadTasks(aCase);
            return MessageResource.successMessage("Task reloaded in case [" + caseId + "]");
        } catch (Exception e) {
            log.error("Reloading tasks of case [{}] failed:", caseId, e);
            return MessageResource.errorMessage("Reloading tasks in case " + caseId + " has failed!");
        }
    }

    @PreAuthorize("@workflowAuthorizationService.canCallDelete(@userService.getLoggedUser(), #caseId)")
    @Operation(summary = "Delete case", security = {@SecurityRequirement(name = "BasicAuth")})
    @DeleteMapping(value = "/case/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<EventOutcomeWithMessage> deleteCase(@PathVariable("id") String caseId, @RequestParam(defaultValue = "false") boolean deleteSubtree) {
        caseId = URLDecoder.decode(caseId, StandardCharsets.UTF_8);
        DeleteCaseEventOutcome outcome;
        if (deleteSubtree) {
            outcome = workflowService.deleteSubtreeRootedAt(caseId);
        } else {
            outcome = workflowService.deleteCase(caseId);
        }
        return EventOutcomeWithMessageResource.successMessage("Case " + caseId + " was deleted",
                LocalisedEventOutcomeFactory.from(outcome, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Download case file field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/case/{id}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String caseId, @RequestParam("fieldId") String fieldId) throws FileNotFoundException {
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

    @Operation(summary = "Download one file from cases file list field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/case/{id}/file/named", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFileByName(@PathVariable("id") String caseId, @RequestParam("fieldId") String fieldId, @RequestParam("fileName") String name) throws FileNotFoundException {
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

    @NotNull
    private PagedModel<CaseResource> getCaseResources(Pageable pageable, PagedResourcesAssembler<Case> assembler, Page<Case> cases, Link selfLink) {
        PagedModel<CaseResource> resources = assembler.toModel(cases, new CaseResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources, Case.class, selfLink.getRel().toString());
        return PagedModel.of(cases.stream().map(CaseResource::new).toList(), new PagedModel.PageMetadata(pageable.getPageSize(), pageable.getPageNumber(), cases.getTotalElements()));
    }
}
