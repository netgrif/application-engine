package com.netgrif.workflow.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.elastic.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
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
import java.util.Map;

@RestController
@RequestMapping("/api/task")
@ConditionalOnProperty(
        value = "nae.task.web.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class TaskController {

    public static final Logger log = LoggerFactory.getLogger(TaskController.class);

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IDataService dataService;

    @Autowired
    private IElasticTaskService searchService;

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<LocalisedTaskResource> getAll(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        Page<Task> page = taskService.getAll(loggedUser, pageable, locale);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAll(auth, pageable, assembler, locale)).withRel("all");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/case", method = RequestMethod.POST)
    public PagedResources<LocalisedTaskResource> getAllByCases(@RequestBody List<String> cases, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByCases(pageable, cases);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAllByCases(cases, pageable, assembler, locale)).withRel("case");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/case/{id}", method = RequestMethod.GET)
    public List<TaskReference> getTasksOfCase(@PathVariable("id") String caseId, Locale locale) {
        return taskService.findAllByCase(caseId, locale);
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public LocalisedTaskResource getOne(@PathVariable("id") String taskId, Locale locale) {
        Task task = taskService.findById(taskId);
        if (task == null)
            return null;
        return new LocalisedTaskResource(new com.netgrif.workflow.workflow.web.responsebodies.Task(task, locale));
    }

    @RequestMapping(value = "/assign/{id}", method = RequestMethod.GET)
    @PreAuthorize("@taskAuthorizationService.canCallAssign(#auth.getPrincipal(), #taskId)")
    public MessageResource assign(Authentication auth, @PathVariable("id") String taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        try {
            taskService.assignTask(loggedUser, taskId);
            return MessageResource.successMessage("LocalisedTask " + taskId + " assigned to " + loggedUser.getFullName());
        } catch (TransitionNotExecutableException e) {
            log.error("Assigning task [" + taskId + "] failed: ", e);
            return MessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    @RequestMapping(value = "/delegate/{id}", method = RequestMethod.POST)
    @PreAuthorize("@taskAuthorizationService.canCallDelegate(#auth.getPrincipal(), #taskId)")
    public MessageResource delegate(Authentication auth, @PathVariable("id") String taskId, @RequestBody Long delegatedId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        try {
            taskService.delegateTask(loggedUser, delegatedId, taskId);
            return MessageResource.successMessage("LocalisedTask " + taskId + " assigned to [" + delegatedId + "]");
        } catch (Exception e) {
            log.error("Delegating task [" + taskId + "] failed: ", e);
            return MessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    @RequestMapping(value = "/finish/{id}", method = RequestMethod.GET)
    @PreAuthorize("@taskAuthorizationService.canCallFinish(#auth.getPrincipal(), #taskId)")
    public MessageResource finish(Authentication auth, @PathVariable("id") String taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        try {
            taskService.finishTask(loggedUser, taskId);
            return MessageResource.successMessage("LocalisedTask " + taskId + " finished");
        } catch (Exception e) {
            log.error("Finishing task [" + taskId + "] failed: ", e);
            return MessageResource.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/cancel/{id}", method = RequestMethod.GET)
    @PreAuthorize("@taskAuthorizationService.canCallCancel(#auth.getPrincipal(), #taskId)")
    public MessageResource cancel(Authentication auth, @PathVariable("id") String taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        try {
            taskService.cancelTask(loggedUser, taskId);
            return MessageResource.successMessage("LocalisedTask " + taskId + " canceled");
        } catch (Exception e) {
            log.error("Canceling task [" + taskId + "] failed: ", e);
            return MessageResource.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/my", method = RequestMethod.GET)
    public PagedResources<LocalisedTaskResource> getMy(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMy(auth, pageable, assembler, locale)).withRel("my");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/my/finished", method = RequestMethod.GET)
    public PagedResources<LocalisedTaskResource> getMyFinished(Pageable pageable, Authentication auth, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMyFinished(pageable, auth, assembler, locale)).withRel("finished");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public PagedResources<LocalisedTaskResource> search(Authentication auth, Pageable pageable, @RequestBody SingleTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = taskService.search(searchBody.getList(), pageable, (LoggedUser) auth.getPrincipal(), operation == MergeFilterOperation.AND);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .search(auth, pageable, searchBody, operation, assembler, locale)).withRel("search");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/search_es", method = RequestMethod.POST)
    public PagedResources<LocalisedTaskResource> searchElastic(Authentication auth, Pageable pageable, @RequestBody SingleTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = searchService.search(searchBody.getList(), (LoggedUser) auth.getPrincipal(), pageable, operation == MergeFilterOperation.AND);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .searchElastic(auth, pageable, searchBody, operation, assembler, locale)).withRel("search_es");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    @PostMapping(value = "/count", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public CountResponse count(@RequestBody SingleTaskSearchRequestAsList query, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, Authentication auth, Locale locale) {
        long count = searchService.count(query.getList(), (LoggedUser)auth.getPrincipal(), operation == MergeFilterOperation.AND);
        return CountResponse.taskCount(count);
    }

    @RequestMapping(value = "/{id}/data", method = RequestMethod.GET)
    public DataGroupsResource getData(@PathVariable("id") String taskId, Locale locale) {
        List<DataGroup> dataGroups = dataService.getDataGroups(taskId, locale);

        return new DataGroupsResource(dataGroups, locale);
    }

    @RequestMapping(value = "/{id}/data", method = RequestMethod.POST)
    @PreAuthorize("@taskAuthorizationService.canCallSaveData(#auth.getPrincipal(), #taskId)")
    public ChangedFieldContainer setData(Authentication auth, @PathVariable("id") String taskId, @RequestBody ObjectNode dataBody) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        return dataService.setData(taskId, dataBody);
    }

    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.POST)
    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    public ChangedFieldByFileFieldContainer saveFile(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId, @RequestParam(value = "file") MultipartFile multipartFile) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        return dataService.saveFile(taskId, fieldId, multipartFile);
    }

    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Resource> getFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, HttpServletResponse response) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByTask(taskId, fieldId);

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

    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.DELETE)
    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    public MessageResource deleteFile(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        if (dataService.deleteFile(taskId, fieldId))
            return MessageResource.successMessage("File in field " + fieldId + " within task " + taskId + " was successfully deleted");
        return MessageResource.errorMessage("File in field " + fieldId + " within task" + taskId + " has failed to delete");
    }

    @RequestMapping(value = "/{id}/files/{field}", method = RequestMethod.POST)
    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    public ChangedFieldByFileFieldContainer saveFiles(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId,
                                                      @RequestParam(value = "files") MultipartFile[] multipartFiles) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        return dataService.saveFiles(taskId, fieldId, multipartFiles);
    }

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

    @RequestMapping(value = "/{id}/file/{field}/{name}", method = RequestMethod.DELETE)
    @PreAuthorize("@taskAuthorizationService.canCallSaveFile(#auth.getPrincipal(), #taskId)")
    public MessageResource deleteNamedFile(Authentication auth, @PathVariable("id") String taskId, @PathVariable("field") String fieldId, @PathVariable("name") String name) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();

        if (dataService.deleteFileByName(taskId, fieldId, name))
            return MessageResource.successMessage("File with name " + name + " in field " + fieldId + " within task " + taskId + " was successfully deleted");
        return MessageResource.errorMessage("File with name " + name + " in field " + fieldId + " within task" + taskId + " has failed to delete");
    }
}