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

    List<Task> findByCases(List<String> cases);

    void createTasks(Case useCase);

    List<Task> findByUser(User user);

    Task findById(String id);

    List<Task> findUserFinishedTasks(User user);

    List<Task> findByPetriNets(List<String> petriNets);

    List<Task> findByTransitions(List<String> transitions);

    void finishTask(Long userId, String taskId) throws Exception;

    void assignTask(User user, String taskId) throws TransitionNotExecutableException;

    List<Field> getData(String taskId);

    void setDataFieldsValues(String taskId, ObjectNode values);

    void cancelTask(Long id, String taskId);

    boolean saveFile(String taskId, String fieldId, MultipartFile multipartFile);

    FileSystemResource getFile(String taskId, String fieldId);

    void delegateTask(String delegatedEmail, String taskId) throws TransitionNotExecutableException;
}
