package com.netgrif.workflow.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.service.interfaces.IFilterService;
import com.netgrif.workflow.workflow.service.interfaces.ITaskService;
import com.netgrif.workflow.workflow.web.requestbodies.CreateFilterBody;
import com.netgrif.workflow.workflow.web.requestbodies.TaskSearchBody;
import com.netgrif.workflow.workflow.web.responsebodies.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.PagedResources;
import org.springframework.hateoas.mvc.ControllerLinkBuilder;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/res/task")
public class TaskController {

    @Autowired
    private ITaskService taskService;

    @Autowired
    private IFilterService filterService;

    @RequestMapping(method = RequestMethod.GET)
    public PagedResources<TaskResource> getAll(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        Page<Task> page = taskService.getAll(loggedUser, pageable);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAll(auth, pageable, assembler)).withRel("all");
        PagedResources<TaskResource> resources = assembler.toResource(page,new TaskResourceAssembler(),selfLink);
        ResourceLinkAssembler.addLinks(resources,Task.class,selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/case", method = RequestMethod.POST)
    public PagedResources<TaskResource> getAllByCases(@RequestBody List<String> cases, Pageable pageable, PagedResourcesAssembler<Task> assembler) {
        Page<Task> page = taskService.findByCases(pageable, cases);

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getAllByCases(cases, pageable, assembler)).withRel("case");
        PagedResources<TaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources,Task.class,selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public TaskResource getOne(@PathVariable("id") String taskId) {
        return new TaskResource(taskService.findById(taskId));
    }

    @RequestMapping(value = "/assign/{id}", method = RequestMethod.GET)
    public MessageResource assign(Authentication auth, @PathVariable("id") String taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            taskService.assignTask(loggedUser.transformToUser(), taskId);
            return MessageResource.successMessage("Task " + taskId + " assigned to " + loggedUser.getFullName());
        } catch (TransitionNotExecutableException e) {
            e.printStackTrace();
            return MessageResource.errorMessage("Task " + taskId + " cannot be assigned");
        }
    }

    @RequestMapping(value = "/delegate/{id}", method = RequestMethod.POST)
    public MessageResource delegate(Authentication auth, @PathVariable("id") String taskId, @RequestBody String delegatedEmail) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            delegatedEmail = URLDecoder.decode(delegatedEmail, StandardCharsets.UTF_8.name());
            taskService.delegateTask(loggedUser.getId(), delegatedEmail, taskId);
            return MessageResource.successMessage("Task " + taskId + " assigned to " + delegatedEmail);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return MessageResource.errorMessage("Task " + taskId + " cannot be assigned");
        }
    }

    @RequestMapping(value = "/finish/{id}", method = RequestMethod.GET)
    public MessageResource finish(Authentication auth, @PathVariable("id") String taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            taskService.finishTask(loggedUser.getId(), taskId);
            return MessageResource.successMessage("Task " + taskId + " finished");
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResource.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/cancel/{id}", method = RequestMethod.GET)
    public MessageResource cancel(Authentication auth, @PathVariable("id") String taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            taskService.cancelTask(loggedUser.getId(), taskId);
            return MessageResource.successMessage("Task " + taskId + " canceled");
        } catch (Exception e) {
            e.printStackTrace();
            return MessageResource.errorMessage(e.getMessage());
        }
    }

    @RequestMapping(value = "/my", method = RequestMethod.GET)
    public PagedResources<TaskResource> getMy(Authentication auth, Pageable pageable, PagedResourcesAssembler<Task> assembler) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMy(auth, pageable, assembler)).withRel("my");
        PagedResources<TaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources,Task.class,selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/my/finished", method = RequestMethod.GET)
    public PagedResources<TaskResource> getMyFinished(Pageable pageable, Authentication auth, PagedResourcesAssembler<Task> assembler) {
        Page<Task> page = taskService.findByUser(pageable, ((LoggedUser) auth.getPrincipal()).transformToUser());

        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .getMyFinished(pageable, auth, assembler)).withRel("finished");
        PagedResources<TaskResource> resources = assembler.toResource(page, new TaskResourceAssembler(), selfLink);
        ResourceLinkAssembler.addLinks(resources,Task.class,selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public PagedResources<TaskResource> search(Authentication auth, Pageable pageable, @RequestBody TaskSearchBody searchBody, PagedResourcesAssembler<Task> assembler) {
        Page<Task> page = null;
        if (searchBody.searchTier == TaskSearchBody.SEARCH_TIER_1) {
            page = taskService.findByPetriNets(pageable, searchBody.petriNets
                    .stream()
                    .map(net -> net.petriNet)
                    .collect(Collectors.toList()));
        } else if (searchBody.searchTier == TaskSearchBody.SEARCH_TIER_2) {
            List<String> transitions = new ArrayList<>();
            searchBody.petriNets.forEach(net -> transitions.addAll(net.transitions));
            page = taskService.findByTransitions(pageable, transitions);
        } else if (searchBody.searchTier == TaskSearchBody.SEARCH_TIER_3) {
            //TODO: 4.6.2017 vyhľadanie na základe dát
        }
        Link selfLink = ControllerLinkBuilder.linkTo(ControllerLinkBuilder.methodOn(TaskController.class)
                .search(auth, pageable, searchBody, assembler)).withRel("search");
        PagedResources<TaskResource> resources = assembler.toResource(page,new TaskResourceAssembler(),selfLink);
        ResourceLinkAssembler.addLinks(resources,Task.class,selfLink.getRel());
        return resources;
    }

    @RequestMapping(value = "/{id}/data", method = RequestMethod.GET)
    public DataFieldsResource getData(@PathVariable("id") String taskId) {
        List<Field> dataFields = taskService.getData(taskId);
        return new DataFieldsResource(dataFields, taskId);
    }

    @RequestMapping(value = "/{id}/data", method = RequestMethod.POST)
    public ObjectNode saveData(@PathVariable("id") String taskId, @RequestBody ObjectNode dataBody) {
        return taskService.setData(taskId, dataBody);
    }

    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.POST)
    public MessageResource saveFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId,
                                    @RequestParam(value = "file") MultipartFile multipartFile) {
        if (taskService.saveFile(taskId, fieldId, multipartFile))
            return MessageResource.successMessage("File " + multipartFile.getOriginalFilename() + " successfully uploaded");
        else
            return MessageResource.errorMessage("File " + multipartFile.getOriginalFilename() + " failed to upload");
    }

    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource getFile(@PathVariable("id") String taskId, @PathVariable("field") String fieldId, HttpServletResponse response) {
        FileSystemResource fileResource = taskService.getFile(taskId, fieldId);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileResource.getFilename().substring(fileResource.getFilename().indexOf('-') + 1));
        return fileResource;
    }

    //TODO: Paged filters resource
    @RequestMapping(value = "/filter", method = RequestMethod.GET)
    public FiltersResource getAllFilters(Authentication auth) {
        return new FiltersResource(filterService.getAll(), false);
    }

    @RequestMapping(value = "/filter/roles", method = RequestMethod.POST)
    public FiltersResource getFiltersWithRoles(@RequestBody List<String> roles) {
        return new FiltersResource(filterService.getWithRoles(roles), true);
    }

    @RequestMapping(value = "/filter", method = RequestMethod.POST)
    public MessageResource saveFilter(Authentication auth, @RequestBody CreateFilterBody filterBody) {
        boolean saveSuccess = filterService.saveFilter(((LoggedUser) auth.getPrincipal()), filterBody);
        if (saveSuccess) {
            return MessageResource.successMessage("Filter " + filterBody.name + " saved");
        } else {
            return MessageResource.errorMessage("Filter " + filterBody.name + " saving failed!");
        }
    }
}