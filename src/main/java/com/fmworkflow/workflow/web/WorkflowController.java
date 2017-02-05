package com.fmworkflow.workflow.web;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.service.ISecurityService;
import com.fmworkflow.auth.service.IUserService;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.Place;
import com.fmworkflow.petrinet.service.IPetriNetService;
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

import javax.websocket.server.PathParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController()
@RequestMapping("/workflow")
public class WorkflowController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private ITaskService taskService;

    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public void createCase(@RequestBody CreateBody body) {
        PetriNet petriNet = petriNetService.loadPetriNet(body.netId);
        Map<String, Integer> activePlaces = new HashMap<>();
        Map<String, Place> places = petriNet.getPlaces();
        for (Place place : places.values()) {
            if (place.getTokens() > 0) {
                activePlaces.put(place.getObjectId().toString(), place.getTokens());
            }
        }
        Case useCase = new Case(body.title);
        useCase.setPetriNet(petriNet);
        useCase.setActivePlaces(activePlaces);

        workflowService.saveCase(useCase);
    }

    @RequestMapping(value = "/all", method = RequestMethod.GET)
    public List<Case> getAll() {
        return workflowService.getAll();
    }

    @RequestMapping(value = "/tasks", method = RequestMethod.POST)
    public List<Task> viewTasks(@RequestBody String caseId) {
        return taskService.findByCaseId(caseId);
    }

    @RequestMapping(value = "/mytasks", method = RequestMethod.GET)
    public List<Task> viewMyTasks() {
        User user = userService.getLoggedInUser();
        return taskService.findByUser(user);
    }

    @RequestMapping(value = "/finish", method = RequestMethod.POST)
    public String finishTask(@RequestBody String taskId) {
        return ""; // TODO: 5. 2. 2017  
    }
}