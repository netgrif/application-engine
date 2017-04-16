package com.fmworkflow.workflow.service.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fmworkflow.auth.domain.LoggedUser;
import com.fmworkflow.auth.domain.User;
import com.fmworkflow.petrinet.domain.dataset.Field;
import com.fmworkflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.Task;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ITaskService {
    List<Task> getAll(LoggedUser loggedUser);

    List<Task> findByCaseId(String caseId);

    void createTasks(Case useCase);

    List<Task> findByUser(User user);

    Task findById(Long id);

    List<Task> findUserFinishedTasks(User user);

    List<Task> findByPetriNets(List<String> petriNets);

    List<Task> findByTransitions(List<String> transitions);

    void finishTask(Long userId, Long taskId) throws Exception;

    void assignTask(User user, Long taskId) throws TransitionNotExecutableException;

    List<Field> getData(Long taskId);

    void setDataFieldsValues(Long taskId, ObjectNode values);

    void cancelTask(Long id, Long taskId);

    boolean saveFile(Long taskId, String fieldId, MultipartFile multipartFile);

    FileSystemResource getFile(Long taskId, String fieldId);

    void delegateTask(String delegatedEmail, Long taskId) throws TransitionNotExecutableException;
}
