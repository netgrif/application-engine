package com.fmworkflow.workflow.service;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fmworkflow.auth.domain.LoggedUser;
import com.fmworkflow.auth.domain.User;
import com.fmworkflow.petrinet.domain.dataset.Field;
import com.fmworkflow.petrinet.domain.throwable.TransitionNotStartableException;
import com.fmworkflow.workflow.domain.Case;
import com.fmworkflow.workflow.domain.Task;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ITaskService {
    List<Task> getAll(LoggedUser loggedUser);

    List<Task> findByCaseId(String caseId);

    void createTasks(Case useCase);

    List<Task> findByUser(User user);

    Task findById(Long id);

    List<Task> findUserFinishedTasks(User user);

    void finishTask(Long userId, Long taskId) throws Exception;

    void assignTask(User user, Long taskId) throws TransitionNotStartableException;

    List<Field> getData(Long taskId);

    void setDataFieldsValues(Long taskId, ObjectNode values);

    void cancelTask(Long id, Long taskId);

    boolean saveFile(Long taskId, String fieldId, MultipartFile multipartFile);

    FileSystemResource getFile(Long taskId, String fieldId);
}
