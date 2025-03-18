package com.netgrif.application.engine.workflow.service.interfaces;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.core.petrinet.domain.dataset.Field;
import com.netgrif.core.petrinet.domain.dataset.FileField;
import com.netgrif.core.petrinet.domain.dataset.FileListField;
import com.netgrif.core.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.files.throwable.StorageException;
import com.netgrif.core.petrinet.domain.dataset.*;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.core.workflow.domain.Task;
import com.netgrif.core.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.core.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.core.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.service.FileFieldInputStream;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface IDataService {

    GetDataEventOutcome getData(String taskId);

    GetDataEventOutcome getData(String taskId, Map<String, String> params);

    GetDataEventOutcome getData(Task task, Case useCase);

    GetDataEventOutcome getData(Task task, Case useCase, Map<String, String> params);

    SetDataEventOutcome setData(String taskId, ObjectNode values);

    SetDataEventOutcome setData(String taskId, ObjectNode values, Map<String, String> params);

    SetDataEventOutcome setData(Task task, ObjectNode values);

    SetDataEventOutcome setData(Task task, ObjectNode values, Map<String, String> params);

    FileFieldInputStream getFile(Case useCase, Task task, FileField field, boolean forPreview) throws FileNotFoundException;

    FileFieldInputStream getFile(Case useCase, Task task, FileField field, boolean forPreview, Map<String, String> params) throws FileNotFoundException;

    FileFieldInputStream getFileByName(Case useCase, FileListField field, String name) throws FileNotFoundException;

    FileFieldInputStream getFileByName(Case useCase, FileListField field, String name, Map<String, String> params) throws FileNotFoundException;

    GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale, Set<String> collectedTaskIds, int level, String parentTaskRefId);

    FileFieldInputStream getFileByTask(String taskId, String fieldId, boolean forPreview) throws FileNotFoundException;

    FileFieldInputStream getFileByTaskAndName(String taskId, String fieldId, String name) throws FileNotFoundException;

    FileFieldInputStream getFileByTaskAndName(String taskId, String fieldId, String name, Map<String, String> params) throws FileNotFoundException;

    FileFieldInputStream getFileByCase(String caseId, Task task, String fieldId, boolean forPreview) throws FileNotFoundException;

    FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name) throws FileNotFoundException;

    FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name, Map<String, String> params) throws FileNotFoundException;

    InputStream download(FileListField field, FileFieldValue name) throws StorageException, FileNotFoundException;

    SetDataEventOutcome saveFile(String taskId, String fieldId, MultipartFile multipartFile);

    SetDataEventOutcome saveFile(String taskId, String fieldId, MultipartFile multipartFile, Map<String, String> params);

    SetDataEventOutcome saveFiles(String taskId, String fieldId, MultipartFile[] multipartFile);

    SetDataEventOutcome saveFiles(String taskId, String fieldId, MultipartFile[] multipartFile, Map<String, String> params);

    SetDataEventOutcome deleteFile(String taskId, String fieldId);

    SetDataEventOutcome deleteFile(String taskId, String fieldId, Map<String, String> params);

    SetDataEventOutcome deleteFileByName(String taskId, String fieldId, String name);

    SetDataEventOutcome deleteFileByName(String taskId, String fieldId, String name, Map<String, String> params);

    GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale);

    Page<Task> setImmediateFields(Page<Task> tasks);

    List<Field<?>> getImmediateFields(Task task);

    UserFieldValue makeUserFieldValue(String id);

    Case applyFieldConnectedChanges(Case useCase, String fieldId);

    Case applyFieldConnectedChanges(Case useCase, Field field);

    void validateCaseRefValue(List<String> value, List<String> allowedNets) throws IllegalArgumentException;

//    void validateTaskRefValue(List<String> value, String restrictedTaskId) throws IllegalArgumentException;

    SetDataEventOutcome changeComponentProperties(Case useCase, String transitionId, String fieldId, Map<String, String> properties);

    SetDataEventOutcome changeComponentProperties(Case useCase, Task task, String fieldId, Map<String, String> properties);

    SetDataEventOutcome changeComponentProperties(Case useCase, String fieldId, Map<String, String> properties);
}