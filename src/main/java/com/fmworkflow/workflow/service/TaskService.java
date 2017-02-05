package com.fmworkflow.workflow.service;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.service.IUserService;
import com.fmworkflow.petrinet.domain.Arc;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.Place;
import com.fmworkflow.petrinet.domain.Transition;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.domain.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sun.text.resources.lt.CollationData_lt;

import java.util.*;

@Service
public class TaskService implements ITaskService {
    @Autowired
    private TaskRepository repository;

    @Override
    public List<Task> findByCaseId(String caseId) {
        return repository.findByCaseId(caseId);
    }

    @Override
    public void createTasks(Case useCase) {
        PetriNet net = useCase.getPetriNet();

        Collection<Transition> transitions = net.getTransitions().values();
        for (Transition transition : transitions) {
            if (isExecutable(transition, net, useCase)) {
                Task task = new Task();
                task.setTitle(transition.getTitle());
                repository.save(task);
            }
        }
    }

    private boolean isExecutable(Transition transition, PetriNet net, Case useCase) {
        Map<String, Integer> activePlaces = useCase.getActivePlaces();
        Collection<Arc> arcsOfTransition = net.getArcsOfTransition(transition);
        for (Arc arc : arcsOfTransition) {
            if (arc.getDestination() == transition) {
                Place source = (Place)arc.getSource();
                if (source.getTokens() < activePlaces.get(source.getObjectId().toString())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public List<Task> findByUser(User user) {
        return repository.findByUser(user);
    }
}