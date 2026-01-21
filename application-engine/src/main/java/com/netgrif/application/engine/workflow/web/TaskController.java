package com.netgrif.application.engine.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleElasticTaskSearchRequestAsList;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessage;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.application.engine.workflow.web.requestbodies.file.FileFieldRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.application.engine.workflow.web.responsebodies.CountResponse;
import com.netgrif.application.engine.workflow.web.responsebodies.LocalisedTaskResource;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/task")
@ConditionalOnProperty(
        value = "netgrif.engine.web.task-enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Task")
public class TaskController extends AbstractTaskController {

    public static final Logger log = LoggerFactory.getLogger(TaskController.class);

    public TaskController(ITaskService taskService,
                          IDataService dataService,
                          IElasticTaskService searchService,
                          IWorkflowService workflowService,
                          UserService userService) {
        super(taskService, dataService, workflowService, searchService, userService);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Get all tasks", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> getAll(Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getAll(pageable, assembler, locale);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Get all tasks by cases", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/case", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> getAllByCases(@RequestBody List<String> cases, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getAllByCases(cases, pageable, assembler, locale);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS')")
    @Operation(summary = "Get tasks of the case", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = {"/case/{id}", "/public/case/{id}"}, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskReference> getTasksOfCase(@PathVariable("id") String caseId, Locale locale) {
        return super.getTasksOfCase(caseId, locale);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Get task by id", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public LocalisedTaskResource getOne(@PathVariable("id") String taskId, Locale locale) {
        return super.getOne(taskId, locale);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS') && @taskAuthorizationService.canCallAssign(@userService.getLoggedUser(), #taskId)")
    @Operation(summary = "Assign task",
            description = "Caller must be able to perform the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = {"/assign/{id}", "/public/assign/{id}"}, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> assign(@PathVariable("id") String taskId, Locale locale) {
        return super.assign(taskId, locale);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN') && @taskAuthorizationService.canCallDelegate(@userService.getLoggedUser(), #taskId)")
    @Operation(summary = "Delegate task",
            description = "Caller must be able to delegate the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/delegate/{id}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> delegate(@PathVariable("id") String taskId, @RequestBody String delegatedId, Locale locale) {
        return super.delegate(taskId, delegatedId, locale);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS') && @taskAuthorizationService.canCallFinish(@userService.getLoggedUser(), #taskId)")
    @Operation(summary = "Finish task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = {"/finish/{id}", "/public/finish/{id}"}, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> finish(@PathVariable("id") String taskId, Locale locale) {
        return super.finish(taskId, locale);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS') && @taskAuthorizationService.canCallCancel(@userService.getLoggedUser(), #taskId)")
    @Operation(summary = "Cancel task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = {"/cancel/{id}", "/public/cancel/{id}"}, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> cancel(@PathVariable("id") String taskId, Locale locale) {
        return super.cancel(taskId, locale);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Get all tasks assigned to logged user", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/my", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> getMy(Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getMy(pageable, assembler, locale);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Get all finished tasks by logged user", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/my/finished", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> getMyFinished(Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getMyFinished(pageable, assembler, locale);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Generic task search on Mongo database", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> search(Pageable pageable, @RequestBody SingleTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation,  PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.search(pageable, searchBody, operation, assembler, locale);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Generic task search on Elasticsearch database", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search_es", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> searchElastic(Pageable pageable, @RequestBody SingleElasticTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation,  PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.searchElastic(pageable, searchBody, operation, assembler, locale);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "Count tasks by provided criteria", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/count", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CountResponse count(@RequestBody SingleElasticTaskSearchRequestAsList query, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Locale locale) {
        return super.count(query, operation, locale);
    }

    @Override
    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS')")
    @Operation(summary = "Get all task data", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = {"/{id}/data", "/public/{id}/data"}, produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<EventOutcomeWithMessage> getData(@PathVariable("id") String taskId, Locale locale) {
        return super.getData(taskId, locale);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS') && @taskAuthorizationService.canCallSaveData(@userService.getLoggedUser(), #taskId)")
    @Operation(summary = "Set task data",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = {"/{id}/data", "/public/{id}/data"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })

    public EntityModel<EventOutcomeWithMessage> setData(@PathVariable("id") String taskId, @RequestBody ObjectNode dataBody, Locale locale) {
        return super.setData(taskId, dataBody, locale);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS') && @taskAuthorizationService.canCallSaveFile(@userService.getLoggedUser(), #taskId)")
    @Operation(summary = "Upload file into the task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = {"/{id}/file", "/public/{id}/file"}, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> saveFile(@PathVariable("id") String taskId, @RequestPart(value = "data") FileFieldRequest dataBody, @RequestPart(value = "file") MultipartFile multipartFile, Locale locale) {
        return super.saveFile(taskId, multipartFile, dataBody, locale);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS')")
    @Operation(summary = "Download task file field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = {"/{id}/file", "/public/{id}/file"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String taskId, @RequestParam("fieldId") String fieldId) throws FileNotFoundException {
        return super.getFile(taskId, fieldId);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS') && @taskAuthorizationService.canCallSaveFile(@userService.getLoggedUser(), #taskId)")
    @Operation(summary = "Remove file from the task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @DeleteMapping(value = {"/{id}/file", "/public/{id}/file"}, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> deleteFile(@PathVariable("id") String taskId, @RequestBody FileFieldRequest requestBody) {
        return super.deleteFile(requestBody.getParentTaskId(), requestBody.getFieldId());
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS') && @taskAuthorizationService.canCallSaveFile(@userService.getLoggedUser(), #taskId)")
    @Operation(summary = "Upload multiple files into the task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = {"/{id}/files", "/public/{id}/files"}, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> saveFiles(@PathVariable("id") String taskId, @RequestPart(value = "data") FileFieldRequest requestBody, @RequestPart(value = "files") MultipartFile[] multipartFiles) {
        return super.saveFiles(taskId, multipartFiles, requestBody);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS')")
    @Operation(summary = "Download one file from tasks file list field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = {"/{id}/file/named", "/public/{id}/file/named"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getNamedFile(@PathVariable("id") String taskId, @RequestParam("fieldId") String fieldId, @RequestParam("fileName") String fileName) throws FileNotFoundException {
        return super.getNamedFile(taskId, fieldId, fileName);
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS') && @taskAuthorizationService.canCallSaveFile(@userService.getLoggedUser(), #taskId)")
    @Operation(summary = "Remove file from tasks file list field value",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @DeleteMapping(value = {"/{id}/file/named", "/public/{id}/file/named"}, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> deleteNamedFile(@PathVariable("id") String taskId, @RequestBody FileFieldRequest requestBody) {
        return super.deleteNamedFile(requestBody.getParentTaskId(), requestBody.getFieldId(), requestBody.getFileName());
    }

    @PreAuthorize("@authorizationService.hasAnyAuthority('USER', 'ADMIN', 'ANONYMOUS')")
    @Operation(summary = "Download preview for file field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = {"/{id}/file_preview", "/public/{id}/file_preview"}, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFilePreview(@PathVariable("id") String taskId, @RequestParam("fieldId") String fieldId) throws FileNotFoundException {
        return super.getFilePreview(taskId, fieldId);
    }
}
