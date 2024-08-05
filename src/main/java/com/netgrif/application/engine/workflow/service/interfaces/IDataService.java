package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.application.engine.workflow.domain.outcomes.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.service.FileFieldInputStream;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface IDataService {

    GetDataEventOutcome getData(String taskId, IUser user);

    GetDataEventOutcome getData(String taskId, IUser user, Map<String, String> params);

    GetDataEventOutcome getData(Task task, Case useCase, IUser user);

    GetDataEventOutcome getData(Task task, Case useCase, IUser user, Map<String, String> params);

    SetDataEventOutcome setData(String taskId, DataSet values, IUser user);

    SetDataEventOutcome setData(String taskId, DataSet values, IUser user, Map<String, String> params);

    SetDataEventOutcome setData(String taskId, DataSet values, LoggedUser loggedUser);

    SetDataEventOutcome setData(String taskId, DataSet values, LoggedUser loggedUser, Map<String, String> params);

    SetDataEventOutcome setData(Case useCase, DataSet dataSet, IUser user);

    SetDataEventOutcome setData(Case useCase, DataSet dataSet, IUser user, Map<String, String> params);

    SetDataEventOutcome setData(Task task, DataSet values, IUser user);

    SetDataEventOutcome setData(Task task, DataSet values, IUser user, Map<String, String> params);

    SetDataEventOutcome setDataField(Task task, String fieldId, Field<?> newDataField, IUser user);
    
    SetDataEventOutcome setDataField(Task task, String fieldId, Field<?> newDataField, IUser user, Map<String, String> params);

    FileFieldInputStream getFile(Case useCase, Task task, FileField field, boolean forPreview);

    FileFieldInputStream getFile(Case useCase, Task task, FileField field, boolean forPreview, Map<String, String> params);

    FileFieldInputStream getFileByName(Case useCase, FileListField field, String name);

    FileFieldInputStream getFileByName(Case useCase, FileListField field, String name, Map<String, String> params);

    FileFieldInputStream getFileByTask(String taskId, String fieldId, boolean forPreview) throws FileNotFoundException;

    FileFieldInputStream getFileByTaskAndName(String taskId, String fieldId, String name);

    FileFieldInputStream getFileByTaskAndName(String taskId, String fieldId, String name, Map<String, String> params);

    FileFieldInputStream getFileByCase(String caseId, Task task, String fieldId, boolean forPreview);

    FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name);

    FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name, Map<String, String> params);

    InputStream download(String url) throws IOException;

    // TODO: release/8.0.0 IUSer?
    SetDataEventOutcome saveFile(String taskId, String fieldId, MultipartFile multipartFile);

    SetDataEventOutcome saveFile(String taskId, String fieldId, MultipartFile multipartFile, Map<String, String> params);

    SetDataEventOutcome saveFiles(String taskId, String fieldId, MultipartFile[] multipartFile);

    SetDataEventOutcome saveFiles(String taskId, String fieldId, MultipartFile[] multipartFile, Map<String, String> params);

    SetDataEventOutcome deleteFile(String taskId, String fieldId);

    SetDataEventOutcome deleteFile(String taskId, String fieldId, Map<String, String> params);

    SetDataEventOutcome deleteFileByName(String taskId, String fieldId, String name);

    SetDataEventOutcome deleteFileByName(String taskId, String fieldId, String name, Map<String, String> params);

    // TODO: release/8.0.0 deprecated by forms
    GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale, IUser user);

    GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale, LoggedUser loggedUser);

    // TODO: release/8.0.0 revision
    Page<Task> setImmediateFields(Page<Task> tasks);

    List<Field<?>> getImmediateFields(Task task);

    UserFieldValue makeUserFieldValue(String id);

    Case applyFieldConnectedChanges(Case useCase, Field<?> field);

    void validateCaseRefValue(List<String> value, List<String> allowedNets) throws IllegalArgumentException;
}