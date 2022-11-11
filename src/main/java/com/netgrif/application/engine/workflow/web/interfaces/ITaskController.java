package com.netgrif.application.engine.workflow.web.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.singleaslist.SingleElasticTaskSearchRequestAsList;
import com.netgrif.application.engine.eventoutcomes.LocalisedEventOutcomeFactory;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.workflow.domain.IllegalArgumentWithChangedFieldsException;
import com.netgrif.application.engine.workflow.domain.MergeFilterOperation;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessage;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.response.EventOutcomeWithMessageResource;
import com.netgrif.application.engine.workflow.service.FileFieldInputStream;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.web.PublicTaskController;
import com.netgrif.application.engine.workflow.web.TaskController;
import com.netgrif.application.engine.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.application.engine.workflow.web.responsebodies.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


@RestController("/api/task")
public interface ITaskController extends IController {

    ITaskService taskService();

    IDataService dataService();

    IElasticTaskService searchService();

    @Operation(summary = "Get all tasks", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(produces = MediaTypes.HAL_JSON_VALUE)
    default PagedModel<LocalisedTaskResource> getAll(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        Page<Task> page = taskService().getAll(loggedUser, pageable, locale);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getAll(auth, pageable, assembler, locale)).withRel("all");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    @Operation(summary = "Get all tasks by cases", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/case", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    default PagedModel<LocalisedTaskResource> getAllByCases(@RequestBody List<String> cases, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService().findByCases(pageable, cases);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getAllByCases(cases, pageable, assembler, locale)).withRel("case");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    @Operation(summary = "Get tasks of the case", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/case/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    default List<TaskReference> getTasksOfCase(@PathVariable("id") String caseId, Locale locale) {
        return taskService().findAllByCase(caseId, locale);
    }

    @Operation(summary = "Get task by id", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}", produces = MediaTypes.HAL_JSON_VALUE)
    default LocalisedTaskResource getOne(@PathVariable("id") String taskId, Locale locale) {
        Task task = taskService().findById(taskId);
        if (task == null)
            return null;
        return new LocalisedTaskResource(new com.netgrif.application.engine.workflow.web.responsebodies.Task(task, locale));
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
    default EntityModel<EventOutcomeWithMessage> assign(Authentication auth, @PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " assigned to " + loggedUser.getFullName(),
                    LocalisedEventOutcomeFactory.from(taskService().assignTask(loggedUser, taskId), locale));
        } catch (TransitionNotExecutableException e) {
            log().error("Assigning task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
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
    default EntityModel<EventOutcomeWithMessage> delegate(Authentication auth, @PathVariable("id") String taskId, @RequestBody String delegatedId, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " assigned to [" + delegatedId + "]",
                    LocalisedEventOutcomeFactory.from(taskService().delegateTask(loggedUser, delegatedId, taskId), locale));
        } catch (Exception e) {
            log().error("Delegating task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
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
    default EntityModel<EventOutcomeWithMessage> finish(Authentication auth, @PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " finished",
                    LocalisedEventOutcomeFactory.from(taskService().finishTask(loggedUser, taskId), locale));
        } catch (Exception e) {
            log().error("Finishing task [" + taskId + "] failed: ", e);
            if (e instanceof IllegalArgumentWithChangedFieldsException) {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(((IllegalArgumentWithChangedFieldsException) e).getOutcome(), locale));
            } else {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
            }
        }
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
    default EntityModel<EventOutcomeWithMessage> cancel(Authentication auth, @PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " canceled",
                    LocalisedEventOutcomeFactory.from(taskService().cancelTask(loggedUser, taskId), locale));
        } catch (Exception e) {
            log().error("Canceling task [" + taskId + "] failed: ", e);
            if (e instanceof IllegalArgumentWithChangedFieldsException) {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(((IllegalArgumentWithChangedFieldsException) e).getOutcome(), locale));
            } else {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
            }
        }
    }

    @Operation(summary = "Get all tasks assigned to logged user", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/my", produces = MediaTypes.HAL_JSON_VALUE)
    default PagedModel<LocalisedTaskResource> getMy(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService().findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getMy(auth, pageable, assembler, locale)).withRel("my");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    @Operation(summary = "Get all finished tasks by logged user", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/my/finished", produces = MediaTypes.HAL_JSON_VALUE)
    default PagedModel<LocalisedTaskResource> getMyFinished(Pageable pageable, Authentication auth, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService().findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getMyFinished(pageable, auth, assembler, locale)).withRel("finished");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    @Operation(summary = "Generic task search on Mongo database", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    default PagedModel<LocalisedTaskResource> search(Authentication auth, Pageable pageable, @RequestBody SingleTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = taskService().search(searchBody.getList(), pageable, (LoggedUser) auth.getPrincipal(), locale, operation == MergeFilterOperation.AND);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .search(auth, pageable, searchBody, operation, assembler, locale)).withRel("search");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    @Operation(summary = "Generic task search on Elasticsearch database", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/search_es", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    default PagedModel<LocalisedTaskResource> searchElastic(Authentication auth, Pageable pageable, @RequestBody SingleElasticTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = searchService().search(searchBody.getList(), (LoggedUser) auth.getPrincipal(), pageable, locale, operation == MergeFilterOperation.AND);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .searchElastic(auth, pageable, searchBody, operation, assembler, locale)).withRel("search_es");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    @Operation(summary = "Count tasks by provided criteria", security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/count", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)    default CountResponse count(@RequestBody SingleElasticTaskSearchRequestAsList query, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Authentication auth, Locale locale) {
        long count = searchService().count(query.getList(), (LoggedUser) auth.getPrincipal(), locale, operation == MergeFilterOperation.AND);
        return CountResponse.taskCount(count);
    }

    @Operation(summary = "Get all task data", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}/data", produces = MediaTypes.HAL_JSON_VALUE)
    default EntityModel<EventOutcomeWithMessage> getData(@PathVariable("id") String taskId, Locale locale) {
        try {
            GetDataGroupsEventOutcome outcome = dataService().getDataGroups(taskId, locale);
            return EventOutcomeWithMessageResource.successMessage("Get data groups successful",
                    LocalisedEventOutcomeFactory.from(outcome, locale));
        } catch (IllegalArgumentWithChangedFieldsException e) {
            log().error("Get data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(e.getOutcome(), locale));
        } catch (Exception e) {
            log().error("Get data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
        }
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
    default EntityModel<EventOutcomeWithMessage> setData(Authentication auth, @PathVariable("id") String taskId, @RequestBody ObjectNode dataBody, Locale locale) {
        try {
            Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
            dataBody.fields().forEachRemaining(it -> outcomes.put(it.getKey(), dataService().setData(it.getKey(), it.getValue().deepCopy())));
            SetDataEventOutcome mainOutcome = taskService().getMainOutcome(outcomes, taskId);
            return EventOutcomeWithMessageResource.successMessage("Data field values have been successfully set",
                    LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
        } catch (IllegalArgumentWithChangedFieldsException e) {
            log().error("Set data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(e.getOutcome(), locale));
        } catch (Exception e) {
            log().error("Set data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
        }
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Upload file into the task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{id}/file/{field}",  produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    default EntityModel<EventOutcomeWithMessage> saveFile(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId,
                                                         @RequestPart(value = "data") Map<String, String> dataBody, @RequestPart(value = "file") MultipartFile multipartFile, Locale locale) {
        try {
            Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
            dataBody.entrySet().forEach(it -> outcomes.put(it.getKey(), dataService().saveFile(it.getKey(), fieldId, multipartFile)));
            SetDataEventOutcome mainOutcome = taskService().getMainOutcome(outcomes, taskId);
            return EventOutcomeWithMessageResource.successMessage("Data field values have been successfully set",
                    LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
        } catch (IllegalArgumentWithChangedFieldsException e) {
            log().error("Set data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(e.getOutcome(), locale));
        } catch (Exception e) {
            log().error("Set data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
        }
    }

    @Operation(summary = "Download task file field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}/file/{field}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    default ResponseEntity<Resource> getFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, HttpServletResponse response) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService().getFileByTask(taskId, fieldId, false);
        if (fileFieldInputStream == null || fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File in field " + fieldId + " within task " + taskId + " was not found!");
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileFieldInputStream.getFileName() + "\"");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Remove file from the task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @DeleteMapping(value = "/{id}/file/{field}", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    default EntityModel<EventOutcomeWithMessage> deleteFile(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId, @RequestParam("parentTaskId") String parentTaskId) {
        Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
        outcomes.put(taskId, dataService().deleteFile(taskId, fieldId));
        SetDataEventOutcome mainOutcome = taskService().getMainOutcome(outcomes, taskId);
        return EventOutcomeWithMessageResource.successMessage("Data field values have been sucessfully set",
                LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Upload multiple files into the task",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @PostMapping(value = "/{id}/files/{field}", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    default EntityModel<EventOutcomeWithMessage> saveFiles(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId,
                                                          @RequestPart(value = "data") Map<String, String> dataBody, @RequestPart(value = "files") MultipartFile[] multipartFiles) {
        Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
        dataBody.entrySet().forEach(it -> outcomes.put(it.getKey(), dataService().saveFiles(it.getKey(), fieldId, multipartFiles)));
        SetDataEventOutcome mainOutcome = taskService().getMainOutcome(outcomes, taskId);
        return EventOutcomeWithMessageResource.successMessage("Data field values have been sucessfully set",
                LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Download one file from tasks file list field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}/file/{field}/{name}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    default ResponseEntity<Resource> getNamedFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, @PathVariable("name") String name,
                                                 HttpServletResponse response) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService().getFileByTaskAndName(taskId, fieldId, name);
        if (fileFieldInputStream == null || fileFieldInputStream.getInputStream() == null) {
            throw new FileNotFoundException("File with name " + name + " in field " + fieldId + " within task " + taskId + " was not found!");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileFieldInputStream.getFileName() + "\"");
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @Operation(summary = "Remove file from tasks file list field value",
            description = "Caller must be assigned to the task, or must be an ADMIN",
            security = {@SecurityRequirement(name = "BasicAuth")})
    @DeleteMapping(value = "/{id}/file/{field}/{name}", produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "403", description = "Caller doesn't fulfill the authorisation requirements"),
    })
    default EntityModel<EventOutcomeWithMessage> deleteNamedFile(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId, @PathVariable("name") String name, @RequestParam("parentTaskId") String parentTaskId) {
        Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
        outcomes.put(taskId, dataService().deleteFileByName(taskId, fieldId, name));
        SetDataEventOutcome mainOutcome = taskService().getMainOutcome(outcomes, taskId);
        return EventOutcomeWithMessageResource.successMessage("Data field values have been sucessfully set",
                LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
    }

    @Operation(summary = "Download preview for file field value", security = {@SecurityRequirement(name = "BasicAuth")})
    @GetMapping(value = "/{id}/file_preview/{field}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    default ResponseEntity<Resource> getFilePreview(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, HttpServletResponse response) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService().getFileByTask(taskId, fieldId, true);
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (fileFieldInputStream != null ? "\"" + fileFieldInputStream.getFileName() + "\"" : "null"));
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(fileFieldInputStream != null ? new InputStreamResource(fileFieldInputStream.getInputStream()) : null);
    }
}
