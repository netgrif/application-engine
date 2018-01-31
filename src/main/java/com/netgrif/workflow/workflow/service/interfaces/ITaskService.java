package com.netgrif.workflow.workflow.service.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.auth.domain.LoggedUser;
import com.netgrif.workflow.auth.domain.User;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import com.netgrif.workflow.workflow.web.responsebodies.TaskReference;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface ITaskService {
    Page<Task> getAll(LoggedUser loggedUser, Pageable pageable, Locale locale);

    Page<Task> search(Map<String, Object> request, Pageable pageable, LoggedUser user);

    Page<Task> findByCases(Pageable pageable, List<String> cases);

    void createTasks(Case useCase);

    Page<Task> findByUser(Pageable pageable, User user);

    Task findById(String id);

    List<Task> findUserFinishedTasks(User user);

    Page<Task> findByPetriNets(Pageable pageable, List<String> petriNets);

    Page<Task> findByTransitions(Pageable pageable, List<String> transitions);

    void finishTask(LoggedUser loggedUser, String taskId) throws Exception;

    void assignTask(LoggedUser loggedUser, String taskId) throws TransitionNotExecutableException;

    List<Field> getData(String taskId);

    List<Field> getData(Task task, Case useCase);

    List<DataGroup> getDataGroups(String taskId);

    ChangedFieldContainer setData(String taskId, ObjectNode values);

    void cancelTask(LoggedUser loggedUser, String taskId);

    void delegateTask(LoggedUser loggedUser, String delegatedEmail, String taskId) throws TransitionNotExecutableException;

    boolean saveFile(String taskId, String fieldId, MultipartFile multipartFile);

    FileSystemResource getFile(String taskId, String fieldId);

    void deleteTasksByCase(String caseId);

    Field buildField(Case useCase, String fieldId, boolean withValidation);

    List<TaskReference> findAllByCase(String caseId, Locale locale);
}