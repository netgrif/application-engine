package com.netgrif.workflow.workflow.service.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ITaskService {
    Page<Task> getAll(LoggedUser loggedUser, Pageable pageable);

    Page<Task> findByCases(Pageable pageable, List<String> cases);

    void createTasks(Case useCase);

    Page<Task> findByUser(Pageable pageable, User user);

    Task findById(String id);

    List<Task> findUserFinishedTasks(User user);

    Page<Task> findByPetriNets(Pageable pageable, List<String> petriNets);

    Page<Task> findByTransitions(Pageable pageable, List<String> transitions);

    void finishTask(Long userId, String taskId) throws Exception;

    void assignTask(User user, String taskId) throws TransitionNotExecutableException;

    List<Field> getData(String taskId);

    void setDataFieldsValues(String taskId, ObjectNode values);

    void cancelTask(Long id, String taskId);

    boolean saveFile(String taskId, String fieldId, MultipartFile multipartFile);

    FileSystemResource getFile(String taskId, String fieldId);

    void delegateTask(String delegatedEmail, String taskId) throws TransitionNotExecutableException;
}