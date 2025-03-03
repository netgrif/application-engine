package com.netgrif.application.engine.workflow.web;

import com.netgrif.application.engine.authentication.domain.LoggedUser;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleElasticTaskSearchRequestAsList;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessage;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.web.requestbodies.file.FileFieldRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.application.engine.workflow.web.responsebodies.CountResponse;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskDataSets;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskResource;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/api/task")
@ConditionalOnProperty(
        value = "nae.task.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Tag(name = "Task")
public class TaskController extends AbstractTaskController {

    public TaskController(ITaskService taskService, IDataService dataService, IElasticTaskService searchService) {
        super(taskService, dataService, searchService);
    }

    @Override
    @Operation(summary = "Get all tasks", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<TaskResource> getAll(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getAll(auth, pageable, assembler, locale);
    }

    @Override
    @Operation(summary = "Get all tasks by cases", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/case", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<TaskResource> getAllByCases(@RequestBody List<String> cases, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getAllByCases(cases, pageable, assembler, locale);
    }

    @Override
    @Operation(summary = "Get tasks of the case", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/case/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskReference> getTasksOfCase(@PathVariable("id") String caseId, Locale locale) {
        return super.getTasksOfCase(caseId, locale);
    }

    @Override
    @Operation(summary = "Get task by id", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    public TaskResource getOne(@PathVariable("id") String taskId, Locale locale) {
        return super.getOne(taskId, locale);
    }

    @PreAuthorize("@taskAuthorizationService.canCallAssign(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Assign task",
            description = "Caller must be able to perform the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/assign/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> assign(Authentication auth, @PathVariable("id") String taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        return super.assign(loggedUser, taskId);
    }

    @PreAuthorize("@taskAuthorizationService.canCallDelegate(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Delegate task",
            description = "Caller must be able to delegate the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/delegate/{id}", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> delegate(Authentication auth, @PathVariable("id") String taskId, @RequestBody String delegatedId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        return super.delegate(loggedUser, taskId, delegatedId);
    }

    @PreAuthorize("@taskAuthorizationService.canCallFinish(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Finish task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/finish/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> finish(Authentication auth, @PathVariable("id") String taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        return super.finish(loggedUser, taskId);
    }

    @PreAuthorize("@taskAuthorizationService.canCallCancel(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Cancel task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/cancel/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> cancel(Authentication auth, @PathVariable("id") String taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        return super.cancel(loggedUser, taskId);
    }

    @Override
    @Operation(summary = "Get all tasks assigned to logged user", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/my", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<TaskResource> getMy(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getMy(auth, pageable, assembler, locale);
    }

    @Override
    @Operation(summary = "Get all finished tasks by logged user", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/my/finished", produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<TaskResource> getMyFinished(Pageable pageable, Authentication auth, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getMyFinished(pageable, auth, assembler, locale);
    }

    @Override
    @Operation(summary = "Generic task search on Mongo database", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<TaskResource> search(Authentication auth, Pageable pageable, @RequestBody SingleTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.search(auth, pageable, searchBody, operation, assembler, locale);
    }

    @Override
    @Operation(summary = "Generic task search on Elasticsearch database", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search_es", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<TaskResource> searchElastic(Authentication auth, Pageable pageable, @RequestBody SingleElasticTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.searchElastic(auth, pageable, searchBody, operation, assembler, locale);
    }

    @Override
    @Operation(summary = "Count tasks by provided criteria", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/count", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CountResponse count(@RequestBody SingleElasticTaskSearchRequestAsList query, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Authentication auth, Locale locale) {
        return super.count(query, operation, auth, locale);
    }

    // TODO: NAE-1969 fix
    @Override
    @Operation(summary = "Get all task data", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}/data", produces = MediaTypes.HAL_JSON_VALUE)
    public EntityModel<EventOutcomeWithMessage> getData(@PathVariable("id") String taskId, Locale locale, Authentication auth) {
        // TODO: release/8.0.0 6.2.5 set order?
        return super.getData(taskId, locale, auth);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveData(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Set task data",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{id}/data", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> setData(Authentication auth, @PathVariable("id") String taskId, @RequestBody TaskDataSets dataBody) {
        return super.setData(taskId, dataBody, auth);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Upload file into the task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{id}/file", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> saveFile(Authentication auth, @PathVariable("id") String taskId, @RequestPart(value = "data") FileFieldRequest dataBody, @RequestPart(value = "file") MultipartFile multipartFile, Locale locale) {
        return super.saveFile(taskId, multipartFile, dataBody, locale);
    }

    @Operation(summary = "Download task file field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String taskId, @RequestParam("fieldId") String fieldId) throws FileNotFoundException {
        return super.getFile(taskId, fieldId);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Remove file from the task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @DeleteMapping(value = "/{id}/file", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> deleteFile(Authentication auth, @PathVariable("id") String taskId, @RequestBody FileFieldRequest requestBody) {
        return super.deleteFile(requestBody.getParentTaskId(), requestBody.getFieldId());
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Upload multiple files into the task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{id}/files", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> saveFiles(Authentication auth, @PathVariable("id") String taskId, @RequestPart(value = "data") FileFieldRequest requestBody, @RequestPart(value = "files") MultipartFile[] multipartFiles) {
        return super.saveFiles(taskId, multipartFiles, requestBody);
    }

    @Operation(summary = "Download one file from tasks file list field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}/file/named", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getNamedFile(@PathVariable("id") String taskId, @RequestParam("fieldId") String fieldId, @RequestParam("fileName") String fileName) throws FileNotFoundException {
        return super.getNamedFile(taskId, fieldId, fileName);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Remove file from tasks file list field value",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @DeleteMapping(value = "/{id}/file/named", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> deleteNamedFile(Authentication auth, @PathVariable("id") String taskId, @RequestBody FileFieldRequest requestBody) {
        return super.deleteNamedFile(requestBody.getParentTaskId(), requestBody.getFieldId(), requestBody.getFileName());
    }

    @Operation(summary = "Download preview for file field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}/file_preview", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFilePreview(@PathVariable("id") String taskId, @RequestParam("fieldId") String fieldId) throws FileNotFoundException {
        return super.getFilePreview(taskId, fieldId);
    }
}
