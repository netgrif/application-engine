package com.netgrif.workflow.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.workflow.elastic.web.requestbodies.singleaslist.SingleElasticTaskSearchRequestAsList;
import com.netgrif.workflow.eventoutcomes.LocalisedEventOutcomeFactory;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.IllegalArgumentWithChangedFieldsException;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.workflow.workflow.domain.eventoutcomes.response.EventOutcomeWithMessageResource;
import com.netgrif.workflow.workflow.service.FileFieldInputStream;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

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

    public EventOutcomeWithMessageResource assign(LoggedUser loggedUser, String taskId, Locale locale) {
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " assigned to " + loggedUser.getFullName(),
                    LocalisedEventOutcomeFactory.from(taskService.assignTask(loggedUser, taskId),locale));
        } catch (TransitionNotExecutableException e) {
            log.error("Assigning task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    public EventOutcomeWithMessageResource delegate(LoggedUser loggedUser, String taskId, String delegatedId, Locale locale) {
        Long userId = delegatedId != null ? Long.parseLong(delegatedId) : null;
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " assigned to [" + userId + "]",
                    LocalisedEventOutcomeFactory.from(taskService.delegateTask(loggedUser, userId, taskId),locale));
        } catch (Exception e) {
            log.error("Delegating task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    public EventOutcomeWithMessageResource finish(LoggedUser loggedUser, String taskId, Locale locale) {

        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " finished",
                    LocalisedEventOutcomeFactory.from(taskService.finishTask(loggedUser, taskId),locale));
        } catch (Exception e) {
            log.error("Finishing task [" + taskId + "] failed: ", e);
            if (e instanceof IllegalArgumentWithChangedFieldsException) {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), ((IllegalArgumentWithChangedFieldsException) e).getChangedFields());
            } else {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
            }
        }
    }

    public EventOutcomeWithMessageResource cancel(LoggedUser loggedUser, String taskId, Locale locale) {
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " canceled",
                    LocalisedEventOutcomeFactory.from(taskService.cancelTask(loggedUser, taskId),locale));
        } catch (Exception e) {
            log.error("Canceling task [" + taskId + "] failed: ", e);
            if (e instanceof IllegalArgumentWithChangedFieldsException) {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), ((IllegalArgumentWithChangedFieldsException) e).getChangedFields());
            } else {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
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

    public PagedResources<LocalisedTaskResource> searchPublic(LoggedUser loggedUser, Pageable pageable, SingleTaskSearchRequestAsList searchBody, MergeFilterOperation operation, PagedResourcesAssembler<com.netgrif.workflow.workflow.domain.Task> assembler, Locale locale) {
        Page<com.netgrif.workflow.workflow.domain.Task> tasks = taskService.search(searchBody.getList(), pageable, loggedUser,locale, operation == MergeFilterOperation.AND);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PublicTaskController.class)
                .searchPublic(loggedUser, pageable, searchBody, operation, assembler, locale)).withRel("search");
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


    public EventOutcomeWithMessageResource getData(String taskId, Locale locale) {
        GetDataGroupsEventOutcome outcome = dataService.getDataGroups(taskId, locale);
        return EventOutcomeWithMessageResource.successMessage("Get data groups successful",
                LocalisedEventOutcomeFactory.from(outcome,locale));
    }

    public EventOutcomeWithMessageResource setData(String taskId, ObjectNode dataBody) {
        Map<String,SetDataEventOutcome> outcomes = new HashMap<>();
        dataBody.fields().forEachRemaining(it -> outcomes.put(it.getKey(), dataService.setData(it.getKey(), it.getValue().deepCopy())));
        SetDataEventOutcome mainOutcome = outcomes.get(taskId);
        outcomes.remove(taskId);
        mainOutcome.addOutcomes(new ArrayList<>(outcomes.values()));
        return EventOutcomeWithMessageResource.successMessage("Data field values have been sucessfully set",
                LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
    }

    public EventOutcomeWithMessageResource saveFile(String taskId, String fieldId, MultipartFile multipartFile) {
        return EventOutcomeWithMessageResource.successMessage("Data field values have been sucessfully set",
                LocalisedEventOutcomeFactory.from(dataService.saveFile(taskId, fieldId, multipartFile), LocaleContextHolder.getLocale()));
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

    public EventOutcomeWithMessageResource saveFiles(String taskId, String fieldId, MultipartFile[] multipartFiles) {
        return EventOutcomeWithMessageResource.successMessage("Data field values have been sucessfully set",
                LocalisedEventOutcomeFactory.from(dataService.saveFiles(taskId, fieldId, multipartFiles), LocaleContextHolder.getLocale()));
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
