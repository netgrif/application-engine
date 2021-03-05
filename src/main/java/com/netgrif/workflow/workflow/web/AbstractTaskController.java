package com.netgrif.workflow.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.elastic.web.requestbodies.singleaslist.SingleElasticTaskSearchRequestAsList;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldByFileFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.IllegalArgumentWithChangedFieldsException;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.FileFieldInputStream;
import com.netgrif.workflow.workflow.service.TaskService;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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

public abstract class AbstractTaskController {

    public static final Logger log = LoggerFactory.getLogger(TaskController.class);

    private final ITaskService taskService;

    private final IDataService dataService;

    private final IElasticTaskService searchService;

    public AbstractTaskController(ITaskService taskService, IDataService dataService, IElasticTaskService searchService) {
        this.taskService = taskService;
        this.dataService = dataService;
        this.searchService = searchService;
    }


    public PagedResources<LocalisedTaskResource> getAll(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        Page<Task> page = taskService.getAll(loggedUser, pageable, locale);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAll(auth, pageable, assembler, locale)).withRel("all");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    public PagedResources<LocalisedTaskResource> getAllByCases(List<String> cases, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByCases(pageable, cases);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAllByCases(cases, pageable, assembler, locale)).withRel("case");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    public List<TaskReference> getTasksOfCase(String caseId, Locale locale) {
        return taskService.findAllByCase(caseId, locale);
    }

    public LocalisedTaskResource getOne(String taskId, Locale locale) {
        Task task = taskService.findById(taskId);
        if (task == null)
            return null;
        return new LocalisedTaskResource(new com.netgrif.workflow.workflow.web.responsebodies.Task(task, locale));
    }

    public LocalisedEventOutcomeResource assign(LoggedUser loggedUser, String taskId, Locale locale) {
        try {
            return LocalisedEventOutcomeResource.successOutcome(taskService.assignTask(loggedUser, taskId), locale,
                    "LocalisedTask " + taskId + " assigned to " + loggedUser.getFullName());
        } catch (TransitionNotExecutableException e) {
            log.error("Assigning task [" + taskId + "] failed: ", e);
            return LocalisedEventOutcomeResource.errorOutcome("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    public LocalisedEventOutcomeResource delegate(LoggedUser loggedUser, String taskId, String delegatedId, Locale locale) {
        Long userId = delegatedId != null ? Long.parseLong(delegatedId) : null;
        try {
            return LocalisedEventOutcomeResource.successOutcome(taskService.delegateTask(loggedUser, userId, taskId), locale,
                    "LocalisedTask " + taskId + " assigned to [" + userId + "]");
        } catch (Exception e) {
            log.error("Delegating task [" + taskId + "] failed: ", e);
            return LocalisedEventOutcomeResource.errorOutcome("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    public LocalisedEventOutcomeResource finish(LoggedUser loggedUser, String taskId, Locale locale) {

        try {
            return LocalisedEventOutcomeResource.successOutcome(taskService.finishTask(loggedUser, taskId), locale,
                    "LocalisedTask " + taskId + " finished");
        } catch (Exception e) {
            log.error("Finishing task [" + taskId + "] failed: ", e);
            if (e instanceof IllegalArgumentWithChangedFieldsException) {
                return LocalisedEventOutcomeResource.errorOutcome(e.getMessage(), ((IllegalArgumentWithChangedFieldsException) e).getChangedFields());
            } else {
                return LocalisedEventOutcomeResource.errorOutcome(e.getMessage());
            }
        }
    }

    public LocalisedEventOutcomeResource cancel(LoggedUser loggedUser, String taskId, Locale locale) {
        try {
            return LocalisedEventOutcomeResource.successOutcome(taskService.cancelTask(loggedUser, taskId), locale,
                    "LocalisedTask " + taskId + " canceled");
        } catch (Exception e) {
            log.error("Canceling task [" + taskId + "] failed: ", e);
            if (e instanceof IllegalArgumentWithChangedFieldsException) {
                return LocalisedEventOutcomeResource.errorOutcome(e.getMessage(), ((IllegalArgumentWithChangedFieldsException) e).getChangedFields());
            } else {
                return LocalisedEventOutcomeResource.errorOutcome(e.getMessage());
            }
        }
    }

    public PagedResources<LocalisedTaskResource> getMy(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMy(auth, pageable, assembler, locale)).withRel("my");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    public PagedResources<LocalisedTaskResource> getMyFinished(Pageable pageable, Authentication auth, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMyFinished(pageable, auth, assembler, locale)).withRel("finished");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    public PagedResources<LocalisedTaskResource> search(Authentication auth, Pageable pageable, SingleTaskSearchRequestAsList searchBody, MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = taskService.search(searchBody.getList(), pageable, (LoggedUser) auth.getPrincipal(), locale, operation == MergeFilterOperation.AND);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .search(auth, pageable, searchBody, operation, assembler, locale)).withRel("search");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    public PagedResources<LocalisedTaskResource> search(LoggedUser loggedUser, Pageable pageable, SingleTaskSearchRequestAsList searchBody, MergeFilterOperation operation, PagedResourcesAssembler<com.netgrif.workflow.workflow.domain.Task> assembler, Locale locale) {
        Page<com.netgrif.workflow.workflow.domain.Task> tasks = taskService.search(searchBody.getList(), pageable, loggedUser,locale, operation == MergeFilterOperation.AND);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PublicTaskController.class)
                .search(loggedUser, pageable, searchBody, operation, assembler, locale)).withRel("search");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    public PagedResources<LocalisedTaskResource> searchElastic(Authentication auth, Pageable pageable, SingleElasticTaskSearchRequestAsList searchBody, MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = searchService.search(searchBody.getList(), (LoggedUser) auth.getPrincipal(), pageable, locale, operation == MergeFilterOperation.AND);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .searchElastic(auth, pageable, searchBody, operation, assembler, locale)).withRel("search_es");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }

    public CountResponse count(SingleElasticTaskSearchRequestAsList query, MergeFilterOperation operation, Authentication auth, Locale locale) {
        long count = searchService.count(query.getList(), (LoggedUser)auth.getPrincipal(), locale, operation == MergeFilterOperation.AND);
        return CountResponse.taskCount(count);
    }


    public DataGroupsResource getData(String taskId, Locale locale) {
        List<DataGroup> dataGroups = dataService.getDataGroups(taskId, locale);
        return new DataGroupsResource(dataGroups, locale);
    }

    public ChangedFieldContainer setData(String taskId, ObjectNode dataBody) {
        return dataService.setData(taskId, dataBody).flatten();
    }

    public ChangedFieldByFileFieldContainer saveFile(String taskId, String fieldId, MultipartFile multipartFile) {
        return dataService.saveFile(taskId, fieldId, multipartFile);
    }

    public ResponseEntity<Resource> getFile(String taskId, String fieldId) throws FileNotFoundException {
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

    public MessageResource deleteFile(String taskId, String fieldId) {
        if (dataService.deleteFile(taskId, fieldId))
            return MessageResource.successMessage("File in field " + fieldId + " within task " + taskId + " was successfully deleted");
        return MessageResource.errorMessage("File in field " + fieldId + " within task" + taskId + " has failed to delete");
    }

    public ChangedFieldByFileFieldContainer saveFiles(String taskId, String fieldId, MultipartFile[] multipartFiles) {
        return dataService.saveFiles(taskId, fieldId, multipartFiles);
    }

    public ResponseEntity<Resource> getNamedFile(String taskId, String fieldId, String name) throws FileNotFoundException {
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

    public MessageResource deleteNamedFile(String taskId, String fieldId, String name) {
        if (dataService.deleteFileByName(taskId, fieldId, name))
            return MessageResource.successMessage("File with name " + name + " in field " + fieldId + " within task " + taskId + " was successfully deleted");
        return MessageResource.errorMessage("File with name " + name + " in field " + fieldId + " within task" + taskId + " has failed to delete");
    }

    public ResponseEntity<Resource> getFilePreview(String taskId, String fieldId) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByTask(taskId, fieldId, true);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (fileFieldInputStream != null ? fileFieldInputStream.getFileName() : "null"));

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(fileFieldInputStream != null ? new InputStreamResource(fileFieldInputStream.getInputStream()) : null);
    }
}
