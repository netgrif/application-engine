package com.netgrif.application.engine.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessage;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.web.requestbodies.file.FileFieldRequest;
import com.netgrif.application.engine.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.application.engine.workflow.web.responsebodies.LocalisedTaskResource;
import com.netgrif.application.engine.workflow.web.responsebodies.TaskReference;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@Tag(name = "Public Task Controller")
@ConditionalOnProperty(
        value = "nae.public.task.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@RequestMapping({"/api/public/task"})
public class PublicTaskController extends AbstractTaskController {

    final IUserService userService;
    private final ITaskService taskService;
    private final IDataService dataService;

    public PublicTaskController(ITaskService taskService, IDataService dataService, IUserService userService) {
        super(taskService, dataService, null);
        this.taskService = taskService;
        this.dataService = dataService;
        this.userService = userService;
    }

    @Override
    @GetMapping(value = "/case/{id}", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Get tasks of the case")
    public List<TaskReference> getTasksOfCase(@PathVariable("id") String caseId, Locale locale) {
        return this.taskService.findAllByCase(caseId, locale);
    }

    @PreAuthorize("@taskAuthorizationService.canCallAssign(@userService.getAnonymousLogged(), #taskId)")
    @GetMapping(value = "/assign/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Assign task", description = "Caller must be able to perform the task, or must be an ADMIN")
    @ApiResponses({@ApiResponse(
            responseCode = "200",
            description = "OK"
    ), @ApiResponse(
            responseCode = "403",
            description = "Caller doesn't fulfill the authorisation requirements"
    )})
    public EntityModel<EventOutcomeWithMessage> assign(@PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = userService.getAnonymousLogged();
        return super.assign(loggedUser, taskId, locale);
    }

    @PreAuthorize("@taskAuthorizationService.canCallFinish(@userService.getAnonymousLogged(), #taskId)")
    @GetMapping(value = "/finish/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Finish task", description = "Caller must be assigned to the task, or must be an ADMIN")
    @ApiResponses({@ApiResponse(
            responseCode = "200",
            description = "OK"
    ), @ApiResponse(
            responseCode = "403",
            description = "Caller doesn't fulfill the authorisation requirements"
    )})
    public EntityModel<EventOutcomeWithMessage> finish(@PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = userService.getAnonymousLogged();
        return super.finish(loggedUser, taskId, locale);
    }

    @PreAuthorize("@taskAuthorizationService.canCallCancel(@userService.getAnonymousLogged(), #taskId)")
    @GetMapping(value = "/cancel/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Cancel task", description = "Caller must be assigned to the task, or must be an ADMIN")
    @ApiResponses({@ApiResponse(
            responseCode = "200",
            description = "OK"
    ), @ApiResponse(
            responseCode = "403",
            description = "Caller doesn't fulfill the authorisation requirements"
    )})
    public EntityModel<EventOutcomeWithMessage> cancel(@PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = userService.getAnonymousLogged();
        return super.cancel(loggedUser, taskId, locale);
    }

    @Override
    @GetMapping(value = "/{id}/data", produces = MediaTypes.HAL_JSON_VALUE)
    @Operation(summary = "Get all task data")
    public EntityModel<EventOutcomeWithMessage> getData(@PathVariable("id") String taskId, Locale locale) {
        return super.getData(taskId, locale);
    }

    @Override
    @PreAuthorize("@taskAuthorizationService.canCallSaveData(@userService.getAnonymousLogged(), #taskId)")
    @PostMapping(value = "/{id}/data", consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @Operation(summary = "Set task data", description = "Caller must be assigned to the task, or must be an ADMIN")
    @ApiResponses({@ApiResponse(
            responseCode = "200",
            description = "OK"
    ), @ApiResponse(
            responseCode = "403",
            description = "Caller doesn't fulfill the authorisation requirements"
    )})
    public EntityModel<EventOutcomeWithMessage> setData(@PathVariable("id") String taskId, @RequestBody ObjectNode dataBody, Locale locale) {
        return super.setData(taskId, dataBody, locale);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(@userService.getAnonymousLogged(), #taskId)")
    @Operation(summary = "Upload file into the task",
            description = "Caller must be assigned to the task, or must be an ADMIN")
    @PostMapping(value = "/{id}/file", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> saveFile(Authentication auth, @PathVariable("id") String taskId, @RequestPart(value = "data") FileFieldRequest dataBody, @RequestPart(value = "file") MultipartFile multipartFile, Locale locale){
        return super.saveFile(taskId, multipartFile, dataBody, locale);
    }

    @Override
    @Operation(summary = "Download task file field value")
    @GetMapping(value = "/{id}/file", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String taskId, @RequestParam("fieldId") String fieldId) throws FileNotFoundException {
        return super.getFile(taskId, fieldId);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(@userService.getAnonymousLogged(), #taskId)")
    @Operation(summary = "Remove file from the task",
            description = "Caller must be assigned to the task, or must be an ADMIN")
    @DeleteMapping(value = "/{id}/file", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> deleteFile(@PathVariable("id") String taskId, @RequestBody FileFieldRequest requestBody) {
        return super.deleteFile(requestBody.getParentTaskId(), requestBody.getFieldId());
    }

    @Override
    @Operation(summary = "Download preview for file field value")
    @GetMapping(value = "/{id}/file_preview", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFilePreview(@PathVariable("id") String taskId, @RequestParam("fieldId") String fieldId) throws FileNotFoundException {
        return super.getFilePreview(taskId, fieldId);
    }

    @Override
    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(@userService.getAnonymousLogged(), #taskId)")
    @Operation(summary = "Upload multiple files into the task",
            description = "Caller must be assigned to the task, or must be an ADMIN")
    @PostMapping(value = "/{id}/files", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> saveFiles(@PathVariable("id") String taskId, @RequestPart(value = "files") MultipartFile[] multipartFiles, @RequestPart(value = "data") FileFieldRequest requestBody) {
        return super.saveFiles(taskId, multipartFiles, requestBody);
    }

    @Override
    @Operation(summary = "Download one file from tasks file list field value")
    @GetMapping(value = "/{id}/file/named", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getNamedFile(@PathVariable("id") String taskId, @RequestParam("fieldId") String fieldId, @RequestParam("fileName") String fileName) throws FileNotFoundException {
        return super.getNamedFile(taskId, fieldId, fileName);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(@userService.getAnonymousLogged(), #taskId)")
    @Operation(summary = "Remove file from tasks file list field value",
            description = "Caller must be assigned to the task, or must be an ADMIN")
    @DeleteMapping(value = "/{id}/file/named", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    public EntityModel<EventOutcomeWithMessage> deleteNamedFile(@PathVariable("id") String taskId, @RequestBody FileFieldRequest requestBody) {
        return super.deleteNamedFile(requestBody.getParentTaskId(), requestBody.getFieldId(), requestBody.getFileName());
    }

    @Operation(summary = "Generic task search on Mongo database")
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> search(Pageable pageable, @RequestBody SingleTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<com.netgrif.application.engine.workflow.domain.Task> assembler, Locale locale) {
        return super.searchPublic(userService.getAnonymousLogged(), pageable, searchBody, operation, assembler, locale);
    }
}
