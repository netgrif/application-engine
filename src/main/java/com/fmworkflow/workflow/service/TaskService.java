package com.fmworkflow.workflow.service;

import com.fmworkflow.auth.domain.User;
import com.fmworkflow.petrinet.domain.Arc;
import com.fmworkflow.petrinet.domain.PetriNet;
import com.fmworkflow.petrinet.domain.Place;
import com.fmworkflow.petrinet.domain.Transition;
import com.fmworkflow.petrinet.domain.throwable.TransitionNotStartableException;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.CaseRepository;
import com.fmworkflow.workflow.domain.Task;
import com.fmworkflow.workflow.domain.TaskRepository;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class TaskService implements ITaskService {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private CaseRepository caseRepository;

    @Override
    public List<Task> getAll(){
        return taskRepository.findAll();
    }

    @Override
    public List<Task> findByCaseId(String caseId) {
        return taskRepository.findByCaseId(caseId);
    }

    @Override
    public Task findById(Long id) {
        return taskRepository.findOne(id);
    }

    @Override
    public void createTasks(Case useCase) {
        PetriNet net = useCase.getPetriNet();
        Collection<Transition> transitions = net.getTransitions().values();

        for (Transition transition : transitions) {
            if (isExecutable(transition, net, useCase)) {
                Task task = new Task();
                task.setTitle(transition.getTitle());
                task.setCaseId(useCase.get_id().toString());
                task.setTransitionId(transition.getObjectId().toString());
                task.setCaseColor(useCase.getColor());
                task.setPriority(transition.getPriority());
                task = taskRepository.save(task);
                task.setVisualId(net.getInitials());
                taskRepository.save(task);
            }
        }
    }

    private boolean isExecutable(Transition transition, PetriNet net, Case useCase) {
        Map<String, Integer> activePlaces = useCase.getActivePlaces();
        Collection<Arc> arcsOfTransition = net.getArcsOfTransition(transition);

        Optional<Arc> notAcrive = arcsOfTransition.stream()
                .filter(arc -> arc.getDestination() == transition)
                .filter(arc -> {
                    Place source = (Place)arc.getSource();
                    return source.getTokens() < activePlaces.get(source.getStringId());
                })
                .findAny();

        return notAcrive.isPresent();
    }

    @Override
    public List<Task> findByUser(User user) {
        return taskRepository.findByUser(user);
    }

    @Override
    public List<Task> findUserFinishedTasks(User user) {
        return taskRepository.findByUserAndFinishDateNotNull(user);
    }

    @Override
    public void finishTask(Long userId, Long taskId) throws Exception {
        Task task = taskRepository.findOne(taskId);
        if(task.getUser().getId().equals(userId)){
            throw new Exception("User that is not assigned tried to finish task");
        }

        Case useCase = caseRepository.findOne(task.getCaseId());
        Transition transition =  useCase.getPetriNet().getTransition(task.getTransitionId());

        useCase.finishTransition(transition);
        task.setFinishDate(DateTime.now());

        caseRepository.save(useCase);
        taskRepository.save(task);

        createTasks(useCase);
    }

    @Override
    public void assignTask(User user, Long taskId) throws TransitionNotStartableException { // TODO: 5. 2. 2017 make transactional
        Task task = taskRepository.findOne(taskId);
        Case useCase = caseRepository.findOne(task.getCaseId());
        Transition transition =  useCase.getPetriNet().getTransition(task.getTransitionId());

        useCase.startTransition(transition);
        task.setUser(user);
        task.setStartDate(DateTime.now());

        caseRepository.save(useCase);
        taskRepository.save(task);
    }
}