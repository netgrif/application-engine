package com.netgrif.application.engine.workflow.web;

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
import com.netgrif.application.engine.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.application.engine.workflow.web.responsebodies.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
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


    public PagedModel<LocalisedTaskResource> getAll(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        Page<Task> page = taskService.getAll(loggedUser, pageable, locale);

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getAll(auth, pageable, assembler, locale)).withRel("all");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    public PagedModel<LocalisedTaskResource> getAllByCases(List<String> cases, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByCases(pageable, cases);

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getAllByCases(cases, pageable, assembler, locale)).withRel("case");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    public List<TaskReference> getTasksOfCase(String caseId, Locale locale) {
        return taskService.findAllByCase(caseId, locale);
    }

    public LocalisedTaskResource getOne(String taskId, Locale locale) {
        Task task = taskService.findById(taskId);
        if (task == null)
            return null;
        return new LocalisedTaskResource(new com.netgrif.application.engine.workflow.web.responsebodies.Task(task, locale));
    }

    public EntityModel<EventOutcomeWithMessage> assign(LoggedUser loggedUser, String taskId, Locale locale) {
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " assigned to " + loggedUser.getFullName(),
                    LocalisedEventOutcomeFactory.from(taskService.assignTask(loggedUser, taskId), locale));
        } catch (TransitionNotExecutableException e) {
            log.error("Assigning task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    public EntityModel<EventOutcomeWithMessage> delegate(LoggedUser loggedUser, String taskId, String delegatedId, Locale locale) {
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " assigned to [" + delegatedId + "]",
                    LocalisedEventOutcomeFactory.from(taskService.delegateTask(loggedUser, delegatedId, taskId), locale));
        } catch (Exception e) {
            log.error("Delegating task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    public EntityModel<EventOutcomeWithMessage> finish(LoggedUser loggedUser, String taskId, Locale locale) {

        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " finished",
                    LocalisedEventOutcomeFactory.from(taskService.finishTask(loggedUser, taskId), locale));
        } catch (Exception e) {
            log.error("Finishing task [" + taskId + "] failed: ", e);
            if (e instanceof IllegalArgumentWithChangedFieldsException) {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(((IllegalArgumentWithChangedFieldsException) e).getOutcome(), locale));
            } else {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
            }
        }
    }

    public EntityModel<EventOutcomeWithMessage> cancel(LoggedUser loggedUser, String taskId, Locale locale) {
        try {
            return EventOutcomeWithMessageResource.successMessage("LocalisedTask " + taskId + " canceled",
                    LocalisedEventOutcomeFactory.from(taskService.cancelTask(loggedUser, taskId), locale));
        } catch (Exception e) {
            log.error("Canceling task [" + taskId + "] failed: ", e);
            if (e instanceof IllegalArgumentWithChangedFieldsException) {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(((IllegalArgumentWithChangedFieldsException) e).getOutcome(), locale));
            } else {
                return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
            }
        }
    }

    public PagedModel<LocalisedTaskResource> getMy(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getMy(auth, pageable, assembler, locale)).withRel("my");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    public PagedModel<LocalisedTaskResource> getMyFinished(Pageable pageable, Authentication auth, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .getMyFinished(pageable, auth, assembler, locale)).withRel("finished");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(page, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    public PagedModel<LocalisedTaskResource> search(Authentication auth, Pageable pageable, SingleTaskSearchRequestAsList searchBody, MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = taskService.search(searchBody.getList(), pageable, (LoggedUser) auth.getPrincipal(), locale, operation == MergeFilterOperation.AND);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .search(auth, pageable, searchBody, operation, assembler, locale)).withRel("search");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    public PagedModel<LocalisedTaskResource> searchPublic(LoggedUser loggedUser, Pageable pageable, SingleTaskSearchRequestAsList searchBody, MergeFilterOperation operation, PagedResourcesAssembler<com.netgrif.application.engine.workflow.domain.Task> assembler, Locale locale) {
        Page<com.netgrif.application.engine.workflow.domain.Task> tasks = taskService.search(searchBody.getList(), pageable, loggedUser, locale, operation == MergeFilterOperation.AND);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(PublicTaskController.class)
                .searchPublic(loggedUser, pageable, searchBody, operation, assembler, locale)).withRel("search");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    public PagedModel<LocalisedTaskResource> searchElastic(Authentication auth, Pageable pageable, SingleElasticTaskSearchRequestAsList searchBody, MergeFilterOperation operation, PagedResourcesAssembler<Task> assembler, Locale locale) {
        Page<Task> tasks = searchService.search(searchBody.getList(), (LoggedUser) auth.getPrincipal(), pageable, locale, operation == MergeFilterOperation.AND);
        Link selfLink = WebMvcLinkBuilder.linkTo(WebMvcLinkBuilder.methodOn(TaskController.class)
                .searchElastic(auth, pageable, searchBody, operation, assembler, locale)).withRel("search_es");
        PagedModel<LocalisedTaskResource> resources = assembler.toModel(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel().toString());
        return resources;
    }

    public CountResponse count(SingleElasticTaskSearchRequestAsList query, MergeFilterOperation operation, Authentication auth, Locale locale) {
        long count = searchService.count(query.getList(), (LoggedUser) auth.getPrincipal(), locale, operation == MergeFilterOperation.AND);
        return CountResponse.taskCount(count);
    }


    public EntityModel<EventOutcomeWithMessage> getData(String taskId, Locale locale) {
        try {
            GetDataGroupsEventOutcome outcome = dataService.getDataGroups(taskId, locale);
            return EventOutcomeWithMessageResource.successMessage("Get data groups successful",
                    LocalisedEventOutcomeFactory.from(outcome, locale));
        } catch (IllegalArgumentWithChangedFieldsException e) {
            log.error("Get data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(e.getOutcome(), locale));
        } catch (Exception e) {
            log.error("Get data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage());
        }
    }

    public EntityModel<EventOutcomeWithMessage> setData(String taskId, ObjectNode dataBody, Locale locale) {
        try {
            Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
            dataBody.fields().forEachRemaining(it -> outcomes.put(it.getKey(), dataService.setData(it.getKey(), it.getValue().deepCopy())));
            SetDataEventOutcome mainOutcome = taskService.getMainOutcome(outcomes, taskId);
            return EventOutcomeWithMessageResource.successMessage("Data field values have been successfully set",
                    LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
        } catch (IllegalArgumentWithChangedFieldsException e) {
            log.error("Set data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(e.getOutcome(), locale));
        } catch (Exception e) {
            log.error("Set data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage());

        }
    }

    public EntityModel<EventOutcomeWithMessage> saveFile(String taskId, String fieldId, MultipartFile multipartFile, Map<String, String> dataBody, Locale locale) {
        try {
            Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
            dataBody.entrySet().forEach(it -> outcomes.put(it.getKey(), dataService.saveFile(it.getKey(), fieldId, multipartFile)));
            SetDataEventOutcome mainOutcome = taskService.getMainOutcome(outcomes, taskId);
            return EventOutcomeWithMessageResource.successMessage("Data field values have been successfully set",
                    LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
        } catch (IllegalArgumentWithChangedFieldsException e) {
            log.error("Set data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage(), LocalisedEventOutcomeFactory.from(e.getOutcome(), locale));
        } catch (Exception e) {
            log.error("Set data on task [" + taskId + "] failed: ", e);
            return EventOutcomeWithMessageResource.errorMessage(e.getMessage());

        }
    }

    public ResponseEntity<Resource> getFile(String taskId, String fieldId) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByTask(taskId, fieldId, false);

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

    public EntityModel<EventOutcomeWithMessage> deleteFile(String taskId, String fieldId) {
        Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
        outcomes.put(taskId, dataService.deleteFile(taskId, fieldId));
        SetDataEventOutcome mainOutcome = taskService.getMainOutcome(outcomes, taskId);
        return EventOutcomeWithMessageResource.successMessage("Data field values have been sucessfully set",
                LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));    }

    public EntityModel<EventOutcomeWithMessage> saveFiles(String taskId, String fieldId, MultipartFile[] multipartFiles, Map<String, String> dataBody) {
        Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
        dataBody.entrySet().forEach(it -> outcomes.put(it.getKey(), dataService.saveFiles(it.getKey(), fieldId, multipartFiles)));
        SetDataEventOutcome mainOutcome = taskService.getMainOutcome(outcomes, taskId);
        return EventOutcomeWithMessageResource.successMessage("Data field values have been sucessfully set",
                LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
    }

    public ResponseEntity<Resource> getNamedFile(String taskId, String fieldId, String name) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByTaskAndName(taskId, fieldId, name);

        if (fileFieldInputStream == null || fileFieldInputStream.getInputStream() == null)
            throw new FileNotFoundException("File with name " + name + " in field " + fieldId + " within task " + taskId + " was not found!");

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileFieldInputStream.getFileName() + "\"");

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(new InputStreamResource(fileFieldInputStream.getInputStream()));
    }

    public EntityModel<EventOutcomeWithMessage> deleteNamedFile(String taskId, String fieldId, String name) {
        Map<String, SetDataEventOutcome> outcomes = new HashMap<>();
        outcomes.put(taskId, dataService.deleteFileByName(taskId, fieldId, name));
        SetDataEventOutcome mainOutcome = taskService.getMainOutcome(outcomes, taskId);
        return EventOutcomeWithMessageResource.successMessage("Data field values have been sucessfully set",
                LocalisedEventOutcomeFactory.from(mainOutcome, LocaleContextHolder.getLocale()));
    }

    public ResponseEntity<Resource> getFilePreview(String taskId, String fieldId) throws FileNotFoundException {
        FileFieldInputStream fileFieldInputStream = dataService.getFileByTask(taskId, fieldId, true);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + (fileFieldInputStream != null ? "\"" + fileFieldInputStream.getFileName() + "\"" : "null"));

        return ResponseEntity
                .ok()
                .headers(headers)
                .body(fileFieldInputStream != null ? new InputStreamResource(fileFieldInputStream.getInputStream()) : null);
    }
}
