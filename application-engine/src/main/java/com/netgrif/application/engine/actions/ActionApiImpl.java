package com.netgrif.application.engine.actions;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.netgrif.application.engine.adapter.spring.actions.ActionApi;
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
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.querydsl.core.types.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
        return dataService.getData(taskId, params);
    }

    @Override
    public SetDataEventOutcome setData(String taskId, Map<String, Map<String, String>> dataSet, Map<String, String> params) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper(); 
        String json = mapper.writeValueAsString(dataSet);
        ObjectNode values = (ObjectNode) mapper.readTree(json);
        return dataService.setData(taskId, values, params);
    }

    @Override
    public Page<Case> searchCases(String processIdentifier, Predicate predicate, Pageable pageable) {
        return workflowService.search(predicate, pageable);
    }

    @Override
    public Page<Case> searchCases(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection) {
        boolean intersect = Boolean.TRUE.equals(isIntersection);
        List<CaseSearchRequest> caseSearchRequests = elasticStringQueries.stream().map(query -> CaseSearchRequest.builder().query(query).build()).toList();
        Locale locale = LocaleContextHolder.getLocale();
        return elasticCaseService.search(caseSearchRequests, pageable, locale, intersect);
    }

    @Override
    public Long countCases(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Boolean isIntersection) {
        boolean intersect = Boolean.TRUE.equals(isIntersection);
        List<CaseSearchRequest> caseSearchRequests = elasticStringQueries.stream().map(query -> CaseSearchRequest.builder().query(query).build()).toList();
        Locale locale = LocaleContextHolder.getLocale();
        return elasticCaseService.count(caseSearchRequests, locale, intersect);
    }

    @Override
    public CreateCaseEventOutcome createCaseByIdentifier(String identifier, String title, String color, AuthPrincipalDto authPrincipalDto, Map<String, String> params) {
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
        return workflowService.deleteCase(DeleteCaseParams.with()
                .useCaseId(caseId)
                .params(params)
                .build());
    }

    @Override
    public Page<Task> searchTasks(String processIdentifier, Predicate predicate, Pageable pageable) {
        return taskService.search(predicate, pageable);
    }

    @Override
    public Page<Task> searchTasks(List<String> elasticStringQueries, AuthPrincipalDto authPrincipalDto, Pageable pageable, Boolean isIntersection) {
        boolean intersect = Boolean.TRUE.equals(isIntersection);
        List<ElasticTaskSearchRequest> taskSearchRequests = elasticStringQueries.stream().map(query -> ElasticTaskSearchRequest.builder().query(query).build()).toList();
        LoggedUser loggedUser = ActorTransformer.toLoggedUser(resolveAbstractUser(authPrincipalDto));
        Locale locale = LocaleContextHolder.getLocale();
        return elasticTaskService.search(taskSearchRequests, loggedUser, pageable, locale, intersect);
    }

    @Override
    public AssignTaskEventOutcome assignTask(String taskId, AuthPrincipalDto authPrincipalDto, Map<String, String> params) throws TransitionNotExecutableException {
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
        return workflowService.findOne(caseId);
    }

    @Override
    public Task findTask(String taskId) {
        return taskService.findOne(taskId);
    }

    @Override
    public Page<User> searchUsers(Predicate predicate, Pageable pageable, String realmId) {
        return userService.search(predicate, pageable, realmId);
    }

    @Override
    public AbstractUser getSystemUser() {
        return userService.getSystem();
    }

    private AbstractUser resolveAbstractUser(AuthPrincipalDto authPrincipalDto) {
        if (authPrincipalDto == null) {
            throw new IllegalArgumentException("AuthPrincipalDto cannot be null.");
        }
        Optional<AbstractUser> userOptional = userService.findUserByUsername(authPrincipalDto.getUsername(), authPrincipalDto.getRealmId());
        return userOptional.orElseThrow(() -> new IllegalArgumentException("User with username [%s] and realm ID [%s] not found".formatted(authPrincipalDto.getUsername(), authPrincipalDto.getRealmId())));
    }
}
