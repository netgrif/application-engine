package com.fmworkflow.workflow.web;

import com.fmworkflow.auth.domain.LoggedUser;
import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.petrinet.domain.throwable.TransitionNotStartableException;
import com.fmworkflow.workflow.web.responsebodies.FiltersResource;
import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.web.responsebodies.TaskResource;
import com.fmworkflow.workflow.web.responsebodies.TasksResource;
import com.fmworkflow.workflow.service.IFilterService;
import com.fmworkflow.workflow.service.ITaskService;
import com.fmworkflow.workflow.web.requestbodies.CreateFilterBody;
import com.fmworkflow.workflow.web.requestbodies.TaskSearchBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/res/task")
public class TaskController {

    @Autowired
    private ITaskService taskService;
    @Autowired
    private IFilterService filterService;

    @RequestMapping(method = RequestMethod.GET)
    public TasksResource getAll(){
        List<TaskResource> resources = new ArrayList<>();
        for(Task task : taskService.getAll()){
            resources.add(TaskResource.createFrom(task, null));
        }

        TasksResource tasksResource = new TasksResource(resources);
        tasksResource.addLinks("all");

        return tasksResource;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public TaskResource getOne(@PathVariable("id") Long taskId){
        return TaskResource.createFrom(taskService.findById(taskId),null);
    }

    @RequestMapping(value = "/assign/{id}", method = RequestMethod.GET)
    public Resource<String> assign(Authentication auth, @PathVariable("id") Long taskId){
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            taskService.assignTask(loggedUser.transformToUser(), taskId);
            return new Resource<>(JsonBuilder.init()
                    .addSuccessMessage("Task "+taskId+" assigned to "+loggedUser.getFullName())
                    .build());

        } catch (TransitionNotStartableException e){
            return new Resource<>(JsonBuilder.init()
                    .addErrorMessage("Task "+taskId+" cannot be assigned")
                    .build());
        }
    }

    @RequestMapping(value = "/finish/{id}", method = RequestMethod.GET)
    public Resource<String> finish(Authentication auth, @PathVariable("id") Long taskId){
        LoggedUser loggedUser = (LoggedUser) auth.getPrincipal();
        try {
            taskService.finishTask(loggedUser.getId(), taskId);
            return new Resource<>(JsonBuilder.init()
                    .addSuccessMessage("Task +"+taskId+" finished")
                    .build());

        } catch (Exception e) {
            e.printStackTrace();
            return new Resource<>(JsonBuilder.init()
                    .addErrorMessage(e.getMessage())
                    .build());
        }
    }

    @RequestMapping(value = "/my", method = RequestMethod.GET)
    public TasksResource getMy(Authentication auth){
        List<TaskResource> resources = new ArrayList<>();
        for(Task task:taskService.findByUser(((LoggedUser)auth.getPrincipal()).transformToUser())){
            resources.add(TaskResource.createFrom(task,auth));
        }

        TasksResource tasksResource = new TasksResource(resources);
        tasksResource.addLinks("my");

        return tasksResource;
    }

    @RequestMapping(value = "/my/finished", method = RequestMethod.GET)
    public TasksResource getMyFinished(Authentication auth){
        List<TaskResource> resources = new ArrayList<>();
        for(Task task:taskService.findByUser(((LoggedUser)auth.getPrincipal()).transformToUser())){
            resources.add(TaskResource.createFrom(task, auth));
        }

        TasksResource tasksResources = new TasksResource(resources);
        tasksResources.addLinks("finished");

        return tasksResources;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public TasksResource search(@RequestBody TaskSearchBody searchBody){
        return getAll(); //TODO: 9.2.2017 - search on tasks according to posted json
    }

    @RequestMapping(value = "/filter", method = RequestMethod.GET)
    public FiltersResource getAllFilters(Authentication auth){
        return new FiltersResource(filterService.getAll());
    }

    @RequestMapping(value = "/filter", method = RequestMethod.POST)
    public Resource<String> saveFilter(Authentication auth, @RequestBody CreateFilterBody filterBody){
        boolean saveSuccess = filterService.saveFilter(((LoggedUser) auth.getPrincipal()),filterBody);
        if(saveSuccess){
            return new Resource<>(JsonBuilder.successMessage("Filter "+filterBody.name+" saved"));
        } else {
            return new Resource<>(JsonBuilder.successMessage("Filter "+filterBody.name+" saving failed!"));
        }
    }
}