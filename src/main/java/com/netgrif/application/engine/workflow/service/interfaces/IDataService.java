package com.netgrif.application.engine.workflow.service.interfaces;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.petrinet.domain.dataset.Field;
import com.netgrif.application.engine.petrinet.domain.dataset.FileField;
import com.netgrif.application.engine.petrinet.domain.dataset.FileListField;
import com.netgrif.application.engine.petrinet.domain.dataset.UserFieldValue;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.GetDataGroupsEventOutcome;
import com.netgrif.application.engine.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.workflow.service.FileFieldInputStream;
import com.netgrif.application.engine.workflow.web.responsebodies.DataSet;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;

public interface IDataService {

    GetDataEventOutcome getData(String taskId, IUser user);

    GetDataEventOutcome getData(Task task, Case useCase, IUser user);

    SetDataEventOutcome setData(String taskId, DataSet values, IUser user);

    SetDataEventOutcome setData(String taskId, DataSet values, LoggedUser loggedUser);

    SetDataEventOutcome setData(Case useCase, DataSet dataSet, IUser user);

    SetDataEventOutcome setData(Task task, DataSet values, IUser user);

    SetDataEventOutcome setDataField(Task task, String fieldId, Field<?> newDataField, IUser user);

    FileFieldInputStream getFile(Case useCase, Task task, FileField field, boolean forPreview);

    FileFieldInputStream getFileByName(Case useCase, FileListField field, String name);

    FileFieldInputStream getFileByTask(String taskId, String fieldId, boolean forPreview) throws FileNotFoundException;

    FileFieldInputStream getFileByTaskAndName(String taskId, String fieldId, String name);

    FileFieldInputStream getFileByCase(String caseId, Task task,  String fieldId, boolean forPreview);

    FileFieldInputStream getFileByCaseAndName(String caseId, String fieldId, String name);

    InputStream download(String url) throws IOException;

    SetDataEventOutcome saveFile(String taskId, String fieldId, MultipartFile multipartFile);

    SetDataEventOutcome saveFiles(String taskId, String fieldId, MultipartFile[] multipartFile);

    SetDataEventOutcome deleteFile(String taskId, String fieldId);

    SetDataEventOutcome deleteFileByName(String taskId, String fieldId, String name);

    GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale, IUser user);

    GetDataGroupsEventOutcome getDataGroups(String taskId, Locale locale, LoggedUser loggedUser);

    Page<Task> setImmediateFields(Page<Task> tasks);

    List<Field<?>> getImmediateFields(Task task);

    UserFieldValue makeUserFieldValue(String id);

    Case applyFieldConnectedChanges(Case useCase, Field<?> field);

    void validateCaseRefValue(List<String> value, List<String> allowedNets) throws IllegalArgumentException;
}