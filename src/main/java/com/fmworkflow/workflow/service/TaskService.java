package com.fmworkflow.workflow.service;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.auth.service.IUserService;
import com.fmworkflow.petrinet.domain.*;
import com.fmworkflow.petrinet.domain.throwable.TransitionNotStartableException;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.CaseRepository;
import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.domain.TaskRepository;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class TaskService implements ITaskService {
    @Autowired
    private IUserService userService;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private CaseRepository caseRepository;

    @Override
    public List<Task> findByCaseId(String caseId) {
        return taskRepository.findByCaseId(caseId);
    }

    @Override
    public void createTasks(Case useCase) {
        PetriNet net = useCase.getPetriNet();
        Collection<Transition> transitions = net.getTransitions().values();

        for (Transition transition : transitions) {
            if (isExecutable(transition, net, useCase)) {
                Task task = new Task();
                task.setTitle(transition.getTitle());
                taskRepository.save(task);
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
        return taskRepository.findByUser(user);
    }

    @Override
    public void finishTask(String taskId) {
        Task task = taskRepository.findOne(Long.valueOf(taskId));
        Case useCase = caseRepository.findOne(task.getCaseId());
        Transition transition =  useCase.getPetriNet().getTransition(task.getTransitionId());

        useCase.finishTransition(transition);
        task.setFinishDate(DateTime.now());

        caseRepository.save(useCase);
        taskRepository.save(task);
    }

    @Override
    public void takeTask(String taskId) throws TransitionNotStartableException { // TODO: 5. 2. 2017 make transactional
        Task task = taskRepository.findOne(Long.valueOf(taskId));
        Case useCase = caseRepository.findOne(task.getCaseId());
        Transition transition =  useCase.getPetriNet().getTransition(task.getTransitionId());
        User user = userService.getLoggedInUser();

        useCase.startTransition(transition);
        task.setUser(user);
        task.setStartDate(DateTime.now());

        caseRepository.save(useCase);
        taskRepository.save(task);
    }
}