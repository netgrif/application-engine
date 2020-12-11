package com.netgrif.workflow.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.elastic.web.requestbodies.singleaslist.SingleElasticTaskSearchRequestAsList;
import com.netgrif.workflow.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldByFileFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.FileFieldInputStream;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/task")
@ConditionalOnProperty(
        value = "nae.task.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
@Api(tags = {"Task"}, authorizations = @Authorization("BasicAuth"))
public class TaskController {

    public static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private IElasticTaskService searchService;

    @ApiOperation(value = "Get all tasks", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<LocalisedTaskResource> getAll(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        Page<Task> page = taskService.getAll(loggedUser, pageable, locale);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAll(auth, pageable, assembler, locale)).withRel("all");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Get all tasks by cases", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<LocalisedTaskResource> getAllByCases(@RequestBody List<String> cases, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByCases(pageable, cases);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAllByCases(cases, pageable, assembler, locale)).withRel("case");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Get tasks of the case", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<TaskReference> getTasksOfCase(@PathVariable("id") String caseId, Locale locale) {
        return taskService.findAllByCase(caseId, locale);
    }

    @ApiOperation(value = "Get task by id", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public LocalisedTaskResource getOne(@PathVariable("id") String taskId, Locale locale) {
        Task task = taskService.findById(taskId);
        if (task == null)
            return null;
        return new LocalisedTaskResource(new com.netgrif.workflow.workflow.web.responsebodies.Task(task, locale));
    }

    @PreAuthorize("@taskAuthorizationService.canCallAssign(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Assign task",
            notes = "Caller must be able to perform the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/assign/{id}", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LocalisedEventOutcomeResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public LocalisedEventOutcomeResource assign(Authentication auth, @PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        try {
            return LocalisedEventOutcomeResource.successOutcome(taskService.assignTask(loggedUser, taskId), locale,
                    "LocalisedTask " + taskId + " assigned to " + loggedUser.getFullName());
        } catch (TransitionNotExecutableException e) {
            log.error("Assigning task [" + taskId + "] failed: ", e);
            return LocalisedEventOutcomeResource.errorOutcome("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    @PreAuthorize("@taskAuthorizationService.canCallDelegate(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Delegate task",
            notes = "Caller must be able to delegate the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/delegate/{id}", method = RequestMethod.POST, consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LocalisedEventOutcomeResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public LocalisedEventOutcomeResource delegate(Authentication auth, @PathVariable("id") String taskId, @RequestBody String delegatedId, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        Long userId = delegatedId != null ? Long.parseLong(delegatedId) : null;

        try {
            return LocalisedEventOutcomeResource.successOutcome(taskService.delegateTask(loggedUser, userId, taskId), locale,
                    "LocalisedTask " + taskId + " assigned to [" + userId + "]");
        } catch (Exception e) {
            log.error("Delegating task [" + taskId + "] failed: ", e);
            return LocalisedEventOutcomeResource.errorOutcome("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    @PreAuthorize("@taskAuthorizationService.canCallFinish(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Finish task",
            notes = "Caller must be assigned to the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/finish/{id}", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LocalisedEventOutcomeResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public LocalisedEventOutcomeResource finish(Authentication auth, @PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        try {
            return LocalisedEventOutcomeResource.successOutcome(taskService.finishTask(loggedUser, taskId), locale,
                    "LocalisedTask " + taskId + " finished");
        } catch (Exception e) {
            log.error("Finishing task [" + taskId + "] failed: ", e);
            return LocalisedEventOutcomeResource.errorOutcome(e.getMessage());
        }
    }

    @PreAuthorize("@taskAuthorizationService.canCallCancel(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Cancel task",
            notes = "Caller must be assigned to the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/cancel/{id}", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = LocalisedEventOutcomeResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public LocalisedEventOutcomeResource cancel(Authentication auth, @PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        try {
            return LocalisedEventOutcomeResource.successOutcome(taskService.cancelTask(loggedUser, taskId), locale,
                    "LocalisedTask " + taskId + " canceled");
        } catch (Exception e) {
            log.error("Canceling task [" + taskId + "] failed: ", e);
            return LocalisedEventOutcomeResource.errorOutcome(e.getMessage());
        }
    }

    @ApiOperation(value = "Get all tasks assigned to logged user", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/my", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<LocalisedTaskResource> getMy(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMy(auth, pageable, assembler, locale)).withRel("my");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Get all finished tasks by logged user", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/my/finished", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<LocalisedTaskResource> getMyFinished(Pageable pageable, Authentication auth, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMyFinished(pageable, auth, assembler, locale)).withRel("finished");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Generic task search on Mongo database", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<LocalisedTaskResource> search(Authentication auth, Pageable pageable, @RequestBody SingleTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = taskService.search(searchBody.getList(), pageable, (LoggedUser) auth.getPrincipal(), locale, operation == MergeFilterOperation.AND);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .search(auth, pageable, searchBody, operation, assembler, locale)).withRel("search");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Generic task search on Elasticsearch database", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/search_es", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<LocalisedTaskResource> searchElastic(Authentication auth, Pageable pageable, @RequestBody SingleElasticTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = searchService.search(searchBody.getList(), (LoggedUser) auth.getPrincipal(), pageable, locale, operation == MergeFilterOperation.AND);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .searchElastic(auth, pageable, searchBody, operation, assembler, locale)).withRel("search_es");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @ApiOperation(value = "Count tasks by provided criteria", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/count", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CountResponse count(@RequestBody SingleElasticTaskSearchRequestAsList query, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Authentication auth, Locale locale) {
        long count = searchService.count(query.getList(), (LoggedUser)auth.getPrincipal(), locale, operation == MergeFilterOperation.AND);
        return CountResponse.taskCount(count);
    }

    @ApiOperation(value = "Get all task data", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/data", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public DataGroupsResource getData(@PathVariable("id") String taskId, Locale locale) {
        List<DataGroup> dataGroups = dataService.getDataGroups(taskId, locale);
        return new DataGroupsResource(dataGroups, locale);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveData(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Set task data",
            notes = "Caller must be assigned to the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/data", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ChangedFieldContainer.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public ChangedFieldContainer setData(Authentication auth, @PathVariable("id") String taskId, @RequestBody ObjectNode dataBody) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        return dataService.setData(taskId, dataBody);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Upload file into the task",
            notes = "Caller must be assigned to the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.POST, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ChangedFieldByFileFieldContainer.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public ChangedFieldByFileFieldContainer saveFile(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId, @RequestParam(value = "file") MultipartFile multipartFile) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        return dataService.saveFile(taskId, fieldId, multipartFile);
    }

    @ApiOperation(value = "Download task file field value", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, HttpServletResponse response) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByTask(taskId, fieldId, false);

        if (fileFieldInputStream == null || fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File in field " + fieldId + " within task " + taskId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileFieldInputStream.getFileName());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Remove file from the task",
            notes = "Caller must be assigned to the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.DELETE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource deleteFile(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        if (dataService.deleteFile(taskId, fieldId))
            return MessageResource.successMessage("File in field " + fieldId + " within task " + taskId + " was successfully deleted");
        return MessageResource.errorMessage("File in field " + fieldId + " within task" + taskId + " has failed to delete");
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Upload multiple files into the task",
            notes = "Caller must be assigned to the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/files/{field}", method = RequestMethod.POST, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ChangedFieldByFileFieldContainer.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public ChangedFieldByFileFieldContainer saveFiles(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId,
                                                      @RequestParam(value = "files") MultipartFile[] multipartFiles) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        return dataService.saveFiles(taskId, fieldId, multipartFiles);
    }

    @ApiOperation(value = "Download one file from tasks file list field value", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/file/{field}/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getNamedFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, @PathVariable("name") String name,
                                                 HttpServletResponse response) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByTaskAndName(taskId, fieldId, name);

        if (fileFieldInputStream == null || fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File with name " + name + " in field " + fieldId + " within task " + taskId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileFieldInputStream.getFileName());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Remove file from tasks file list field value",
            notes = "Caller must be assigned to the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/file/{field}/{name}", method = RequestMethod.DELETE, produces = MediaTypes.HAL_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = MessageResource.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public MessageResource deleteNamedFile(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId, @PathVariable("name") String name) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        if (dataService.deleteFileByName(taskId, fieldId, name))
            return MessageResource.successMessage("File with name " + name + " in field " + fieldId + " within task " + taskId + " was successfully deleted");
        return MessageResource.errorMessage("File with name " + name + " in field " + fieldId + " within task" + taskId + " has failed to delete");
    }

    @ApiOperation(value = "Download preview for file field value", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/file_preview/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFilePreview(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, HttpServletResponse response) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByTask(taskId, fieldId, true);

        if (fileFieldInputStream == null || fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File in field " + fieldId + " within task " + taskId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileFieldInputStream.getFileName());

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }
}