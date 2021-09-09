package com.netgrif.workflow.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.elastic.web.requestbodies.singleaslist.SingleElasticTaskSearchRequestAsList;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldByFileFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
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
public class TaskController extends AbstractTaskController {

    public static final Logger log = LoggerFactory.getLogger(TaskController.class);

    public TaskController(ITaskService taskService, IDataService dataService, IElasticTaskService searchService) {
        super(taskService, dataService, searchService);
    }

    @Override
    @ApiOperation(value = "Get all tasks", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> getAll(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getAll(auth, pageable, assembler, locale);
    }

    @Override
    @ApiOperation(value = "Get all tasks by cases", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> getAllByCases(@RequestBody List<String> cases, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getAllByCases(cases, pageable, assembler, locale);
    }

    @Override
    @ApiOperation(value = "Get tasks of the case", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/case/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<TaskReference> getTasksOfCase(@PathVariable("id") String caseId, Locale locale) {
        return super.getTasksOfCase(caseId, locale);
    }

    @Override
    @ApiOperation(value = "Get task by id", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public LocalisedTaskResource getOne(@PathVariable("id") String taskId, Locale locale) {
        return super.getOne(taskId, locale);
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
        return super.assign(loggedUser, taskId, locale);
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
        return super.delegate(loggedUser, taskId, delegatedId, locale);
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
        return super.finish(loggedUser, taskId, locale);
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
        return super.cancel(loggedUser, taskId, locale);
    }

    @Override
    @ApiOperation(value = "Get all tasks assigned to logged user", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/my", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> getMy(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getMy(auth, pageable, assembler, locale);
    }

    @Override
    @ApiOperation(value = "Get all finished tasks by logged user", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/my/finished", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> getMyFinished(Pageable pageable, Authentication auth, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.getMyFinished(pageable, auth, assembler, locale);
    }

    @Override
    @ApiOperation(value = "Generic task search on Mongo database", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/search", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> search(Authentication auth, Pageable pageable, @RequestBody SingleTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.search(auth, pageable, searchBody, operation, assembler, locale);
    }

    @Override
    @ApiOperation(value = "Generic task search on Elasticsearch database", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/search_es", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedModel<LocalisedTaskResource> searchElastic(Authentication auth, Pageable pageable, @RequestBody SingleElasticTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        return super.searchElastic(auth, pageable, searchBody, operation, assembler, locale);
    }

    @Override
    @ApiOperation(value = "Count tasks by provided criteria", authorizations = @Authorization("BasicAuth"))
    @PostMapping(value = "/count", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public CountResponse count(@RequestBody SingleElasticTaskSearchRequestAsList query, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Authentication auth, Locale locale) {
        return super.count(query, operation, auth, locale);
    }

    @Override
    @ApiOperation(value = "Get all task data", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/data", method = RequestMethod.GET, produces = MediaTypes.HAL_JSON_VALUE)
    public DataGroupsResource getData(@PathVariable("id") String taskId, Locale locale) {
        return super.getData(taskId, locale);
    }

    @PreAuthorize("@taskAuthorizationService.canCallSaveData(#auth.getPrincipal(), #taskId)")
    @ApiOperation(value = "Set task data",
            notes = "Caller must be assigned to the task, or must be an ADMIN",
            authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/data", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "OK", response = ChangedFieldContainer.class),
            @ApiResponse(code = 403, message = "Caller doesn't fulfill the authorisation requirements"),
    })
    public ChangedFieldContainer setData(Authentication auth, @PathVariable("id") String taskId, @RequestBody ObjectNode dataBody) {
        return super.setData(taskId, dataBody);
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
        return super.saveFile(taskId, fieldId, multipartFile);
    }

    @ApiOperation(value = "Download task file field value", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, HttpServletResponse response) throws FileNotFoundException {
        return super.getFile(taskId, fieldId);
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

        return super.deleteFile(taskId, fieldId);
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

        return super.saveFiles(taskId, fieldId, multipartFiles);
    }

    @ApiOperation(value = "Download one file from tasks file list field value", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/file/{field}/{name}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getNamedFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, @PathVariable("name") String name,
                                                 HttpServletResponse response) throws FileNotFoundException {
        return super.getNamedFile(taskId, fieldId, name);
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
        return super.deleteNamedFile(taskId, fieldId, name);
    }

    @ApiOperation(value = "Download preview for file field value", authorizations = @Authorization("BasicAuth"))
    @RequestMapping(value = "/{id}/file_preview/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFilePreview(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, HttpServletResponse response) throws FileNotFoundException {
        return super.getFilePreview(taskId, fieldId);
    }
}