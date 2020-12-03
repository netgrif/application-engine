package com.netgrif.workflow.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.MergeFilterOperation;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.IDataService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.requestbodies.singleaslist.SingleTaskSearchRequestAsList;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping({"/api/public"})
@Slf4j
public class PublicTaskController extends PublicAbstractController {

    private final ITaskService taskService;

    private final IDataService dataService;

    public PublicTaskController(ITaskService taskService, IDataService dataService, IUserService userService) {
        super(userService);
        this.taskService = taskService;
        this.dataService = dataService;
    }

    @GetMapping(value = "/task/case/{id}", produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "Get tasks of the case")
    public List<TaskReference> getTasksOfCase(@PathVariable("id") String caseId, Locale locale) {
        return this.taskService.findAllByCase(caseId, locale);
    }

    @GetMapping(value = "/task/assign/{id}", produces = "application/hal+json")
    @ApiOperation(value = "Assign task", notes = "Caller must be able to perform the task, or must be an ADMIN")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "OK",
            response = LocalisedEventOutcomeResource.class
    ), @ApiResponse(
            code = 403,
            message = "Caller doesn't fulfill the authorisation requirements"
    )})
    public LocalisedEventOutcomeResource assign(@PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = getAnonym();
        try {
            return LocalisedEventOutcomeResource.successOutcome(this.taskService.assignTask(loggedUser, taskId), locale, "LocalisedTask " + taskId + " assigned to " + loggedUser.getFullName());
        } catch (TransitionNotExecutableException var6) {
            log.error("Assigning task [" + taskId + "] failed: ", var6);
            return LocalisedEventOutcomeResource.errorOutcome("LocalisedTask " + taskId + " cannot be assigned");
        }
    }

    @GetMapping(value = "/task/finish/{id}", produces = "application/hal+json")
    @ApiOperation(value = "Finish task", notes = "Caller must be assigned to the task, or must be an ADMIN")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "OK",
            response = LocalisedEventOutcomeResource.class
    ), @ApiResponse(
            code = 403,
            message = "Caller doesn't fulfill the authorisation requirements"
    )})
    public LocalisedEventOutcomeResource finish(@PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = getAnonym();
        try {
            return LocalisedEventOutcomeResource.successOutcome(this.taskService.finishTask(loggedUser, taskId), locale, "LocalisedTask " + taskId + " finished");
        } catch (Exception var6) {
            log.error("Finishing task [" + taskId + "] failed: ", var6);
            return LocalisedEventOutcomeResource.errorOutcome(var6.getMessage());
        }
    }

    @GetMapping(value = "/task/cancel/{id}", produces = "application/hal+json")
    @ApiOperation(value = "Cancel task", notes = "Caller must be assigned to the task, or must be an ADMIN")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "OK",
            response = LocalisedEventOutcomeResource.class
    ), @ApiResponse(
            code = 403,
            message = "Caller doesn't fulfill the authorisation requirements"
    )})
    public LocalisedEventOutcomeResource cancel(@PathVariable("id") String taskId, Locale locale) {
        LoggedUser loggedUser = getAnonym();
        try {
            return LocalisedEventOutcomeResource.successOutcome(this.taskService.cancelTask(loggedUser, taskId), locale, "LocalisedTask " + taskId + " canceled");
        } catch (Exception var6) {
            log.error("Canceling task [" + taskId + "] failed: ", var6);
            return LocalisedEventOutcomeResource.errorOutcome(var6.getMessage());
        }
    }

    @GetMapping(value = "/task/{id}/data", produces = "application/hal+json")
    @ApiOperation(value = "Get all task data")
    public DataGroupsResource getData(@PathVariable("id") String taskId, Locale locale) {
        List<DataGroup> dataGroups = this.dataService.getDataGroups(taskId, locale);
        return new DataGroupsResource(dataGroups, locale);
    }

    @PostMapping(value = "/task/{id}/data", consumes = "application/json;charset=UTF-8", produces = "application/json;charset=UTF-8")
    @ApiOperation(value = "Set task data", notes = "Caller must be assigned to the task, or must be an ADMIN")
    @ApiResponses({@ApiResponse(
            code = 200,
            message = "OK",
            response = ChangedFieldContainer.class
    ), @ApiResponse(
            code = 403,
            message = "Caller doesn't fulfill the authorisation requirements"
    )})
    public ChangedFieldContainer setData(@PathVariable("id") String taskId, @RequestBody ObjectNode dataBody) {
        return this.dataService.setData(taskId, dataBody);
    }

    @ApiOperation(value = "Generic task search on Mongo database")
    @PostMapping(value = "/task/search", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaTypes.HAL_JSON_VALUE)
    public PagedResources<LocalisedTaskResource> search(Pageable pageable, @RequestBody SingleTaskSearchRequestAsList searchBody, @RequestParam(defaultValue = "OR") MergeFilterOperation operation, PagedResourcesAssembler<com.netgrif.workflow.workflow.domain.Task> assembler, Locale locale) {
        Page<com.netgrif.workflow.workflow.domain.Task> tasks = taskService.search(searchBody.getList(), pageable, getAnonym(),locale, operation == MergeFilterOperation.AND);
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(PublicTaskController.class)
                .search(pageable, searchBody, operation, assembler, locale)).withRel("search");
        PagedResources<LocalisedTaskResource> resources = assembler.toResource(tasks, new TaskResourceAssembler(locale), selfLink);
        ResourceLinkAssembler.addLinks(resources, Task.class, selfLink.getRel());
        return resources;
    }


}
