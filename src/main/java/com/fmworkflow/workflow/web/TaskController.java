package com.fmworkflow.workflow.web;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fmworkflow.auth.domain.LoggedUser;
import com.fmworkflow.petrinet.domain.dataset.Field;
import com.fmworkflow.petrinet.domain.throwable.TransitionNotStartableException;
import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.service.IFilterService;
import com.fmworkflow.workflow.service.ITaskService;
import com.fmworkflow.workflow.web.requestbodies.CreateFilterBody;
import com.fmworkflow.workflow.web.requestbodies.TaskSearchBody;
import com.fmworkflow.workflow.web.responsebodies.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
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
    public TasksResource getAll(Authentication auth) {
        List<TaskResource> resources = new ArrayList<>();
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        for (Task task : taskService.getAll(loggedUser)) {
            resources.add(TaskResource.createFrom(task, null));
        }

        TasksResource tasksResource = new TasksResource(resources);
        tasksResource.addLinks("all");

        return tasksResource;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public TaskResource getOne(@PathVariable("id") Long taskId) {
        return TaskResource.createFrom(taskService.findById(taskId), null);
    }

    @RequestMapping(value = "/assign/{id}", method = RequestMethod.GET)
    public MessageResource assign(Authentication auth, @PathVariable("id") Long taskId) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            taskService.assignTask(loggedUser.transformToUser(), taskId);
            return MessageResource.successMessage("Task " + taskId + " assigned to " + loggedUser.getFullName());
        } catch (TransitionNotStartableException e) {
            return MessageResource.errorMessage("Task " + taskId + " cannot be assigned");
        }
    }

    @RequestMapping(value = "/delegate/{id}", method = RequestMethod.POST)
    public MessageResource delegate(Authentication auth, @PathVariable("id") Long taskId, @RequestBody String delegatedEmail) {
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            delegatedEmail = URLDecoder.decode(delegatedEmail, StandardCharsets.UTF_8.name());
            taskService.delegateTask(delegatedEmail, taskId);
            return MessageResource.successMessage("Task " + taskId + " assigned to " + delegatedEmail);
        } catch (Exception ignored) {
            ignored.printStackTrace();
            return MessageResource.errorMessage("Task " + taskId + " cannot be assigned");
        }
    }

    @RequestMapping(value = "/finish/{id}", method = RequestMethod.GET)
    public MessageResource finish(Authentication auth, @PathVariable("id") Long taskId) {
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
    public MessageResource cancel(Authentication auth, @PathVariable("id") Long taskId) {
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
    public TasksResource getMy(Authentication auth) {
        List<TaskResource> resources = new ArrayList<>();
        for (Task task : taskService.findByUser(((LoggedUser) auth.getPrincipal()).transformToUser())) {
            resources.add(TaskResource.createFrom(task, auth));
        }

        TasksResource tasksResource = new TasksResource(resources);
        tasksResource.addLinks("my");

        return tasksResource;
    }

    @RequestMapping(value = "/my/finished", method = RequestMethod.GET)
    public TasksResource getMyFinished(Authentication auth) {
        List<TaskResource> resources = new ArrayList<>();
        for (Task task : taskService.findByUser(((LoggedUser) auth.getPrincipal()).transformToUser())) {
            resources.add(TaskResource.createFrom(task, auth));
        }

        TasksResource tasksResources = new TasksResource(resources);
        tasksResources.addLinks("finished");

        return tasksResources;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public TasksResource search(Authentication auth, @RequestBody TaskSearchBody searchBody) {
        List<TaskResource> resources = new ArrayList<>();
        if(searchBody.searchTier == TaskSearchBody.SEARCH_TIER_1){
            taskService.findByPetriNets(searchBody.petriNets
                    .stream()
                    .map(net -> net.petriNet)
                    .collect(Collectors.toList()))
                    .forEach(task -> resources.add(TaskResource.createFrom(task,auth)));
        } else if (searchBody.searchTier == TaskSearchBody.SEARCH_TIER_2){
            List<String> transitions = new ArrayList<>();
            searchBody.petriNets.forEach(net -> transitions.addAll(net.transitions));
            taskService.findByTransitions(transitions).forEach(task -> resources.add(TaskResource.createFrom(task,auth)));
        } else if (searchBody.searchTier == TaskSearchBody.SEARCH_TIER_3){

        }

        TasksResource taskResources = new TasksResource(resources);
        taskResources.addLinks("search");
        return taskResources;
    }

    @RequestMapping(value = "/{id}/data", method = RequestMethod.GET)
    public DataFieldsResource getData(@PathVariable("id") Long taskId) {
        List<Field> dataFields = taskService.getData(taskId);
        return new DataFieldsResource(dataFields, taskId);
    }

    @RequestMapping(value = "/{id}/data", method = RequestMethod.POST)
    public MessageResource saveData(@PathVariable("id") Long taskId, @RequestBody ObjectNode dataBody) {
        taskService.setDataFieldsValues(taskId, dataBody);
        return MessageResource.successMessage("Data for task " + taskId + " saved");
    }

    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.POST)
    public MessageResource saveFile(@PathVariable("id") Long taskId, @PathVariable("field") String fieldId,
                                    @RequestParam(value = "file") MultipartFile multipartFile) {
        if (taskService.saveFile(taskId, fieldId, multipartFile))
            return MessageResource.successMessage("File " + multipartFile.getOriginalFilename() + " successfully uploaded");
        else
            return MessageResource.errorMessage("File " + multipartFile.getOriginalFilename() + " failed to upload");
    }

    @RequestMapping(value = "/{id}/file/{field}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource getFile(@PathVariable("id") Long taskId, @PathVariable("field") String fieldId, HttpServletResponse response) {
        FileSystemResource fileResource = taskService.getFile(taskId, fieldId);
        response.setContentType(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        response.setHeader("Content-Disposition", "attachment; filename=" + fileResource.getFilename().substring(fileResource.getFilename().indexOf('-') + 1));
        return fileResource;
    }

    @RequestMapping(value = "/filter", method = RequestMethod.GET)
    public FiltersResource getAllFilters(Authentication auth) {
        return new FiltersResource(filterService.getAll());
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