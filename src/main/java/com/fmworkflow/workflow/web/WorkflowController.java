package com.fmworkflow.workflow.web;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.service.IUserService;
import com.fmworkflow.json.JsonBuilder;
import com.fmworkflow.petrinet.domain.throwable.TransitionNotStartableException;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.service.ITaskService;
import com.fmworkflow.workflow.service.IWorkflowService;
import com.fmworkflow.workflow.web.requestbodies.CreateBody;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController()
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private ITaskService taskService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public String createCase(@RequestBody CreateBody body) {
        try {
            workflowService.createCase(body.netId, body.title);
            return JsonBuilder.successMessage("Case created successfully");
        } catch (Exception e) { // TODO: 5. 2. 2017 change to custom exception
            e.printStackTrace();
            return JsonBuilder.errorMessage("Failed to create case");
        }
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Case> getAll() {
        return workflowService.getAll();
    }

    @RequestMapping(value = "/tasks", method = RequestMethod.POST)
    public List<Task> viewTasks(@RequestBody String caseId) {
        return taskService.findByCaseId(caseId);
    }

    @RequestMapping(value = "/taketask", method = RequestMethod.POST)
    public String takeTask(@RequestBody String taskId) {
        try {
            taskService.takeTask(taskId);
            return JsonBuilder.successMessage("Task taken");
        } catch (TransitionNotStartableException tnse) {
            tnse.printStackTrace();
            return JsonBuilder.errorMessage("Task cannot be taken. Please check available tasks again.");
        } catch (Exception e) { // TODO: 5. 2. 2017 change to custom exception
            e.printStackTrace();
            return JsonBuilder.errorMessage("Cannot take task");
        }
    }

    @RequestMapping(value = "/mytasks", method = RequestMethod.GET)
    public List<Task> viewMyTasks() {
        User user = userService.getLoggedInUser();
        return taskService.findByUser(user);
    }

    @RequestMapping(value = "/finish", method = RequestMethod.POST)
    public String finishTask(@RequestBody String taskId) {
        try {
            taskService.finishTask(taskId);
            return JsonBuilder.successMessage("Task finished successfully");
        } catch (Exception e) {
            e.printStackTrace();
            return JsonBuilder.errorMessage("Task cannot be finished");
        }
    }
}