package com.netgrif.workflow.workflow.service.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.workflow.petrinet.domain.DataGroup;
import com.netgrif.workflow.petrinet.domain.Transition;
import com.netgrif.workflow.petrinet.domain.dataset.Field;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedField;
import com.netgrif.workflow.petrinet.domain.dataset.logic.ChangedFieldContainer;
import com.netgrif.workflow.petrinet.domain.dataset.logic.action.Action;
import com.netgrif.workflow.workflow.domain.Case;
import com.netgrif.workflow.workflow.domain.Task;
import org.springframework.core.io.FileSystemResource;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface IDataService {

    List<Field> getData(String taskId);

    List<Field> getData(Task task, Case useCase);

    ChangedFieldContainer setData(String taskId, ObjectNode values);

    FileSystemResource getFile(String taskId, String fieldId);

    boolean saveFile(String taskId, String fieldId, MultipartFile multipartFile);

    List<DataGroup> getDataGroups(String taskId);

    Page<Task> setImmediateFields(Page<Task> tasks);

    List<Field> getImmediateFields(Task task);

    Map<String, ChangedField> runActions(List<Action> actions, Case useCase, Transition transition);
}