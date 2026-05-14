package com.netgrif.application.engine.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.adapter.spring.actions.ActionApi;
import com.netgrif.application.engine.adapter.spring.actions.ActionFileHolder;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticTaskService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.elastic.web.requestbodies.ElasticTaskSearchRequest;
import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.objects.auth.domain.User;
import com.netgrif.application.engine.objects.auth.dto.AuthPrincipalDto;
import com.netgrif.application.engine.objects.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.objects.workflow.domain.Case;
import com.netgrif.application.engine.objects.workflow.domain.Task;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.CreateCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.caseoutcomes.DeleteCaseEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.GetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.dataoutcomes.SetDataEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.AssignTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.CancelTaskEventOutcome;
import com.netgrif.application.engine.objects.workflow.domain.eventoutcomes.taskoutcomes.FinishTaskEventOutcome;
import com.netgrif.application.engine.workflow.params.CreateCaseParams;
import com.netgrif.application.engine.workflow.params.DeleteCaseParams;
import com.netgrif.application.engine.workflow.params.TaskParams;
import com.netgrif.application.engine.workflow.service.FileFieldInputStream;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@Slf4j
public class ActionApiImpl implements ActionApi {

    private UserService userService;

    private IDataService dataService;

    private ITaskService taskService;

    private IWorkflowService workflowService;

    private IElasticCaseService elasticCaseService;

    private IElasticTaskService elasticTaskService;

    @Autowired
    public void setDataService(IDataService dataService) {
        this.dataService = dataService;
    }

    @Autowired
    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setWorkflowService(IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Autowired
    public void setElasticCaseService(IElasticCaseService elasticCaseService) {
        this.elasticCaseService = elasticCaseService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setElasticTaskService(IElasticTaskService elasticTaskService) {
        this.elasticTaskService = elasticTaskService;
    }

    @Override
    public GetDataEventOutcome getData(String taskId, Map<String, String> params) {
        log.debug("Getting data for task [{}] with params: [{}]", taskId, params == null ? "null" : params.toString());
        return dataService.getData(taskId, params);
    }

    @Override
    public SetDataEventOutcome setData(String taskId, Map<String, Map<String, String>> dataSet, Map<String, String> params) throws JsonProcessingException {
        log.debug("Setting data for task [{}] with params: [{}]", taskId, params == null ? "null" : params.toString());
        ObjectMapper mapper = new ObjectMapper(); 
        String json = mapper.writeValueAsString(dataSet);
        ObjectNode values = (ObjectNode) mapper.readTree(json);
        log.trace("Setting data for task [{}] with params: [{}], values: [{}]", taskId, params == null ? "null" : params.toString(), values.toString());
        return dataService.setData(taskId, values, params);
    }

    @Override
    public Page<Case> searchCases(String processIdentifier, Predicate predicate, Pageable pageable) {
        return searchCases(processIdentifier, predicate, pageable, new HashMap<>());
    }

    @Override
    public Page<Case> searchCases(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection) {
        return searchCases(elasticStringQueries, authPrincipalDto, pageable, isIntersection, new HashMap<>());
    }

    @Override
    public Long countCases(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Boolean isIntersection) {
        return countCases(elasticStringQueries, authPrincipalDto, isIntersection, new HashMap<>());
    }

    @Override
    public Page<Case> searchCases(String processIdentifier, Predicate predicate, Pageable pageable, Map<String, String> params) {
        log.debug("Searching cases for process identifier [{}] with predicate [{}], pageable [{}]", processIdentifier, predicate, pageable);
        return workflowService.search(predicate, pageable);
    }

    @Override
    public Page<Case> searchCases(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection, Map<String, String> params) {
        log.debug("Searching cases for elastic queries [{}] with auth principal [{}], pageable [{}], intersect [{}]", elasticStringQueries, authPrincipalDto, pageable, isIntersection);
        boolean intersect = Boolean.TRUE.equals(isIntersection);
        List<CaseSearchRequest> caseSearchRequests = elasticStringQueries.stream().map(query -> CaseSearchRequest.builder().query(query).build()).toList();
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(resolveAbstractUser(authPrincipalDto));
        Locale locale = LocaleContextHolder.getLocale();
        log.trace("Searching cases for elastic queries [{}] with auth principal [{}], pageable [{}], intersect [{}], locale [{}]", elasticStringQueries, authPrincipalDto, pageable, isIntersection, locale);
        return elasticCaseService.search(caseSearchRequests, loggedUser, pageable, locale, intersect);
    }

    @Override
    public Long countCases(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Boolean isIntersection, Map<String, String> params) {
        log.debug("Counting cases for elastic queries [{}] with auth principal [{}], intersect [{}]", elasticStringQueries, authPrincipalDto, isIntersection);
        boolean intersect = Boolean.TRUE.equals(isIntersection);
        List<CaseSearchRequest> caseSearchRequests = elasticStringQueries.stream().map(query -> CaseSearchRequest.builder().query(query).build()).toList();
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(resolveAbstractUser(authPrincipalDto));
        Locale locale = LocaleContextHolder.getLocale();
        log.trace("Counting cases for elastic queries [{}] with auth principal [{}], intersect [{}], locale [{}]", elasticStringQueries, authPrincipalDto, isIntersection, locale);
        return elasticCaseService.count(caseSearchRequests, loggedUser, locale, intersect);
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, AuthPrincipalDto authPrincipalDto, Map<String, String> params) {
        log.debug("Creating case with identifier [{}] and title [{}] and color [{}] with auth principal [{}] and params [{}]", identifier, title, color, authPrincipalDto, params);
        Locale locale = LocaleContextHolder.getLocale();
        return workflowService.createCase(CreateCaseParams.with()
                .processIdentifier(identifier)
                .title(title)
                .color(color)
                .author(resolveAbstractUser(authPrincipalDto))
                .locale(locale)
                .params(params)
                .build());
    }

    @Override
    public DeleteCaseEventOutcome deleteCase(String caseId, Map<String, String> params) {
        log.debug("Deleting case with id [{}] and params [{}]", caseId, params);
        return workflowService.deleteCase(DeleteCaseParams.with()
                .useCaseId(caseId)
                .params(params)
                .build());
    }

    @Override
    public Page<Task> searchTasks(String processIdentifier, Predicate predicate, Pageable pageable) {
        return searchTasks(processIdentifier, predicate, pageable, new HashMap<>());
    }

    @Override
    public Page<Task> searchTasks(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection) {
        return searchTasks(elasticStringQueries, authPrincipalDto, pageable, isIntersection, new HashMap<>());
    }

    @Override
    public Page<Task> searchTasks(String processIdentifier, Predicate predicate, Pageable pageable, Map<String, String> params) {
        log.debug("Searching tasks for process identifier [{}] with predicate [{}], pageable [{}]", processIdentifier, predicate, pageable);
        return taskService.search(predicate, pageable);
    }

    @Override
    public Page<Task> searchTasks(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection, Map<String, String> params) {
        log.debug("Searching tasks for elastic queries [{}] with auth principal [{}], pageable [{}], intersect [{}]", elasticStringQueries, authPrincipalDto, pageable, isIntersection);
        boolean intersect = Boolean.TRUE.equals(isIntersection);
        List<ElasticTaskSearchRequest> taskSearchRequests = elasticStringQueries.stream().map(query -> ElasticTaskSearchRequest.builder().query(query).build()).toList();
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(resolveAbstractUser(authPrincipalDto));
        Locale locale = LocaleContextHolder.getLocale();
        log.trace("Searching tasks for elastic queries [{}] with auth principal [{}], pageable [{}], intersect [{}], locale [{}]", elasticStringQueries, authPrincipalDto, pageable, isIntersection, locale);
        return elasticTaskService.search(taskSearchRequests, loggedUser, pageable, locale, intersect);
    }

    @Override
    public Long countTasks(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Boolean isIntersection, Map<String, String> params) {
        log.debug("Counting tasks for elastic queries [{}] with auth principal [{}], intersect [{}]", elasticStringQueries, authPrincipalDto, isIntersection);
        boolean intersect = Boolean.TRUE.equals(isIntersection);
        List<ElasticTaskSearchRequest> taskSearchRequests = elasticStringQueries.stream().map(query -> ElasticTaskSearchRequest.builder().query(query).build()).toList();
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(resolveAbstractUser(authPrincipalDto));
        Locale locale = LocaleContextHolder.getLocale();
        log.trace("Counting tasks for elastic queries [{}] with auth principal [{}], intersect [{}], locale [{}]", elasticStringQueries, authPrincipalDto, isIntersection, locale);
        return elasticTaskService.count(taskSearchRequests, loggedUser, locale, intersect);
    }

    @Override
    public AssignTaskEventOutcome assignTask(String taskId, AuthPrincipalDto authPrincipalDto, Map<String, String> params) throws TransitionNotExecutableException {
        log.debug("Assigning task [{}] with auth principal [{}] and params [{}]", taskId, authPrincipalDto, params);
        Task task = taskService.findOne(taskId);
        AbstractUser user = resolveAbstractUser(authPrincipalDto);
        return taskService.assignTask(TaskParams.with()
                .task(task)
                .user(user)
                .params(params)
                .build());
    }

    @Override
    public CancelTaskEventOutcome cancelTask(String taskId, AuthPrincipalDto authPrincipalDto, Map<String, String> params) {
        log.debug("Canceling task [{}] with auth principal [{}] and params [{}]", taskId, authPrincipalDto, params);
        Task task = taskService.findOne(taskId);
        AbstractUser user = resolveAbstractUser(authPrincipalDto);
        return taskService.cancelTask(TaskParams.with()
                .task(task)
                .user(user)
                .params(params)
                .build());
    }

    @Override
    public FinishTaskEventOutcome finishTask(String taskId, AuthPrincipalDto authPrincipalDto, Map<String, String> params) throws TransitionNotExecutableException {
        log.debug("Finishing task [{}] with auth principal [{}] and params [{}]", taskId, authPrincipalDto, params);
        Task task = taskService.findOne(taskId);
        AbstractUser user = resolveAbstractUser(authPrincipalDto);
        return taskService.finishTask(TaskParams.with()
                .task(task)
                .user(user)
                .params(params)
                .build());
    }

    @Override
    public Case findCase(String caseId) {
        log.debug("Finding case with id [{}]", caseId);
        return workflowService.findOne(caseId);
    }

    @Override
    public Task findTask(String taskId) {
        log.debug("Finding task with id [{}]", taskId);
        return taskService.findOne(taskId);
    }

    @Override
    public Page<User> searchUsers(Predicate predicate, Pageable pageable, String realmId) {
        log.debug("Searching users with predicate [{}] and pageable [{}] and realm ID [{}]", predicate, pageable, realmId);
        return userService.search(predicate, pageable, realmId);
    }

    @Override
    public AbstractUser getSystemUser() {
        return userService.getSystem();
    }

    @Override
    public SetDataEventOutcome saveFile(String taskId, String fieldId, ActionFileHolder file, Map<String, String> params) {
        log.debug("Saving file [{}] for task [{}] and field [{}] with params [{}]", file.getFileName(), taskId, fieldId, params);
        MultipartFile multipartFile = new MockMultipartFile(file.getFileName(), file.getFileName(), null, file.getFileContent());
        log.trace("Saving file [{}] for task [{}] and field [{}] with params [{}]", multipartFile.getOriginalFilename(), taskId, fieldId, params);
        return dataService.saveFile(taskId, fieldId, multipartFile, params);
    }

    @Override
    public SetDataEventOutcome saveFiles(String taskId, String fieldId, ActionFileHolder[] files, Map<String, String> params) {
        log.debug("Saving files [{}] for task [{}] and field [{}] with params [{}]", files.length, taskId, fieldId, params);
        MultipartFile[] multipartFiles = new MultipartFile[files.length];
        for (int i = 0; i < files.length; i++) {
            multipartFiles[i] = new MockMultipartFile(files[i].getFileName(), files[i].getFileName(), null, files[i].getFileContent());
            log.trace("Saving file [{}] for task [{}] and field [{}] with params [{}]", multipartFiles[i].getOriginalFilename(), taskId, fieldId, params);
        }
        log.trace("Saving files [{}] for task [{}] and field [{}] with params [{}]", multipartFiles.length, taskId, fieldId, params);
        return dataService.saveFiles(taskId, fieldId, multipartFiles, params);
    }

    @Override
    public SetDataEventOutcome deleteFile(String taskId, String fieldId, Map<String, String> params) {
        log.debug("Deleting file for task [{}] and field [{}] with params [{}]", taskId, fieldId, params);
        return dataService.deleteFile(taskId, fieldId, params);
    }

    @Override
    public SetDataEventOutcome deleteFileByName(String taskId, String fieldId, String name, Map<String, String> params) {
        log.debug("Deleting file [{}] for task [{}] and field [{}] with params [{}]", name, taskId, fieldId, params);
        return dataService.deleteFileByName(taskId, fieldId, name, params);
    }

    @Override
    public ActionFileHolder getFile(String caseId, String fieldId, Boolean forPreview, Map<String, String> params) throws IOException {
        log.debug("Getting file for case [{}] and field [{}] with preview [{}] and params [{}]", caseId, fieldId, forPreview, params);
        FileFieldInputStream fileFieldInputStream = dataService.getFile(caseId, fieldId, forPreview, params);
        try (InputStream inputStream = fileFieldInputStream.getInputStream()) {
            return ActionFileHolder.builder()
                    .fileName(fileFieldInputStream.getFileName())
                    .fileContent(IOUtils.toByteArray(inputStream))
                    .build();
        }
    }

    @Override
    public ActionFileHolder getFileByCaseAndName(String caseId, String fieldId, String name, Map<String, String> params) throws IOException {
        log.debug("Getting file [{}] for case [{}] and field [{}] with params [{}]", name, caseId, fieldId, params);
        FileFieldInputStream fileFieldInputStream = dataService.getFileByCaseAndName(caseId, fieldId, name, params);
        try (InputStream inputStream = fileFieldInputStream.getInputStream()) {
            return ActionFileHolder.builder()
                    .fileName(fileFieldInputStream.getFileName())
                    .fileContent(IOUtils.toByteArray(inputStream))
                    .build();
        }
    }

    private AbstractUser resolveAbstractUser(AuthPrincipalDto authPrincipalDto) {
        if (authPrincipalDto == null) {
            throw new IllegalArgumentException("AuthPrincipalDto cannot be null.");
        }
        Optional<AbstractUser> userOptional = userService.findUserByUsername(authPrincipalDto.getUsername(), authPrincipalDto.getRealmId());
        return userOptional.orElseThrow(() -> new IllegalArgumentException("User with username [%s] and realm ID [%s] not found".formatted(authPrincipalDto.getUsername(), authPrincipalDto.getRealmId())));
    }
}
