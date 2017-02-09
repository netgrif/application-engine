package com.fmworkflow.workflow.web;

import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.domain.TaskResource;
import com.fmworkflow.workflow.domain.TasksResource;
import com.fmworkflow.workflow.service.ITaskService;
import com.fmworkflow.workflow.web.requestbodies.TaskSearchBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/res/task")
public class TaskController {

    @Autowired
    private ITaskService taskService;

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
    public String assign(Authentication auth, @PathVariable("id") Long taskId){
        return null;
    }

    @RequestMapping(value = "/finish/{id}", method = RequestMethod.GET)
    public String finish(Authentication auth, @PathVariable("id") Long taskId){
        return null;
    }

    @RequestMapping(value = "/my")
    public TasksResource getMy(Authentication auth){
        return null;
    }

    @RequestMapping(value = "/my/finished")
    public TasksResource getMyFinished(Authentication auth){
        return null;
    }

    @RequestMapping(value = "/search", method = RequestMethod.POST)
    public TasksResource search(@RequestBody TaskSearchBody searchBody){
        return null;
    }


}
