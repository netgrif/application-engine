package com.netgrif.application.engine.integration.plugins.service;

import com.netgrif.application.engine.configuration.ApplicationContextProvider;
import com.netgrif.application.engine.integration.plugins.exceptions.PluginDoesNotExistException;
import com.netgrif.core.auth.domain.IUser;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.auth.service.UserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.integration.plugin.injector.PluginInjector;
import com.netgrif.application.engine.integration.plugins.exceptions.PluginIsAlreadyActiveException;
import com.netgrif.application.engine.integration.plugins.properties.PluginConfigProperties;
import com.netgrif.application.engine.integration.plugins.utils.PluginConstants;
import com.netgrif.application.engine.integration.plugins.utils.PluginUtils;
import com.netgrif.core.petrinet.domain.dataset.FieldType;
import com.netgrif.core.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.core.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.pluginlibrary.core.domain.EntryPoint;
import com.netgrif.pluginlibrary.core.domain.Method;
import com.netgrif.pluginlibrary.core.domain.Plugin;
import com.netgrif.pluginlibrary.core.outcomes.CreateOrUpdateOutcome;
import com.netgrif.pluginlibrary.core.outcomes.GetOrCreateOutcome;
import com.netgrif.pluginlibrary.core.service.PluginExecutionService;
import com.netgrif.pluginlibrary.core.service.PluginService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.integration.plugins.utils.PluginConstants.*;
import static com.netgrif.application.engine.integration.plugins.utils.PluginUtils.*;

/**
 * Base service, that manages gRPC server on application startup, registers, activates and deactivates plugins, sends
 * plugin execution requests to desired plugin.
 */
@Slf4j
@Getter
public class PluginServiceImpl implements PluginService {
    private PluginConfigProperties properties;
    private IElasticCaseService elasticCaseService;
    private IWorkflowService workflowService;
    private UserService userService;
    private IDataService dataService;
    private ITaskService taskService;
    private PluginInjector pluginInjector;
    private PluginUtils utils;

    private static final String LOG_PREFIX = "[gRPC Server] -";

    @Autowired
    public void setProperties(PluginConfigProperties properties) {
        this.properties = properties;
    }

    @Autowired
    public void setElasticCaseService(IElasticCaseService elasticCaseService) {
        this.elasticCaseService = elasticCaseService;
    }

    @Autowired
    public void setWorkflowService(IWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @Autowired
    public void setDataService(IDataService dataService) {
        this.dataService = dataService;
    }

    @Autowired
    public void setTaskService(ITaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setPluginInjector(PluginInjector pluginInjector) {
        this.pluginInjector = pluginInjector;
    }

    @Autowired
    public void setUtils(PluginUtils utils) {
        this.utils = utils;
    }

    /**
     * Registers provided plugin into repository
     *
     * @param plugin - plugin to be registered
     * @return registration string message is returned
     */
    @Override
    public String register(Plugin plugin) {
        Case pluginCase;
        try {
            pluginCase = createOrUpdatePluginCase(plugin, Optional.empty());
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        pluginInjector.inject(pluginCase);

        String responseMsg = String.format("Plugin with identifier \"%s\" was registered", getPluginIdentifier(pluginCase));
        log.info(responseMsg);
        return responseMsg;
    }

    /**
     * Activates provided plugin.
     *
     * @param plugin - plugin to be activated
     * @return activation  string message is returned
     */
    @Override
    public String activate(Plugin plugin) throws PluginDoesNotExistException {
        Optional<Case> existingPluginOpt = findByIdentifier(plugin.getIdentifier());
        try {
            return activate(existingPluginOpt.orElseThrow(() -> new PluginDoesNotExistException("Plugin with identifier [%s] cannot be found.".formatted(plugin.getIdentifier()))), plugin);
        } catch (TransitionNotExecutableException | NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unregisters plugin by identifier from memory and database
     *
     * @param identifier - identifier of the plugin to be unregistered
     * @return unregistration string message is returned
     */
    @Override
    public String unregister(String identifier) {
        Optional<Case> existingPluginOpt = findByIdentifier(identifier);
        if (existingPluginOpt.isEmpty()) {
            throw new IllegalArgumentException("Plugin with identifier \"" + identifier + "\" cannot be found");
        }

        pluginInjector.uninject(existingPluginOpt.get());

        removePluginCase(existingPluginOpt.get());

        String responseMsg = "Plugin with identifier \"" + identifier + "\" was unregistered.";
        log.info(responseMsg);
        return responseMsg;
    }

    /**
     * Calls method with arguments of a specified entry point. Plugin must exist and be activated.
     *
     * @param identifier plugin identifier
     * @param entryPoint name of entry point in plugin, that contains the method to be executed
     * @param method     name of method to be executed
     * @param args       arguments to send to plugin method. All args should be the exact type of method input arguments type (not superclass, or subclass)
     * @return the returned object of the executed plugin method
     */
    @Override
    public Object call(String identifier, String entryPoint, String method, Serializable... args) throws IllegalArgumentException {
        Optional<Case> existingPluginOpt = findByIdentifier(identifier);
        if (existingPluginOpt.isEmpty()) {
            throw new IllegalArgumentException("Plugin with identifier \"" + identifier + "\" is not registered.");
        }
        PluginExecutionService pluginExecutionService = ApplicationContextProvider.getAppContext().getBean(PluginExecutionService.class);
        try {
            return pluginExecutionService.execute(entryPoint, method, List.of(args));
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            log.error(LOG_PREFIX + " Failed to execute method [{}] on entryPoint [{}] in plugin [{}]", method, entryPoint, identifier);
            throw new RuntimeException(e);
        }
    }

    /**
     * Deactivates the plugin of the provided identifier
     *
     * @param identifier Identifier of the plugin, that should be deactivated.
     */
    @Override
    public String deactivate(String identifier) throws IllegalArgumentException {
        Optional<Case> pluginOpt = findByIdentifier(identifier);
        if (pluginOpt.isEmpty()) {
            throw new IllegalArgumentException("Plugin with identifier \"" + identifier + "\" cannot be found");
        }
        if (isPluginActive(pluginOpt.get())) {
            String taskId = findTaskIdInCase(pluginOpt.get(), PLUGIN_DEACTIVATE_TRANS_ID);
            IUser user = userService.getLoggedOrSystem();
            try {
                Task deactivateTask = utils.safelyAssignTask(taskId).getTask();
                taskService.finishTask(deactivateTask, user);
                pluginInjector.uninject(pluginOpt.get());
            } catch (TransitionNotExecutableException e) {
                throw new RuntimeException(e);
            }
        }
        String responseMsg = "Plugin with identifier \"" + identifier + "\" was deactivated.";
        log.info(responseMsg);
        return responseMsg;
    }

    /**
     * Finds all plugins in the database
     *
     * @return list of plugin cases
     */
    @Override
    public List<Case> findAll() {
        String query = String.format("processIdentifier:%s", PLUGIN_PROCESS_IDENTIFIER);
        List<CaseSearchRequest> requestAsList = List.of(CaseSearchRequest.builder().query(query).build());
        LoggedUser loggedUser = userService.getLoggedOrSystem().transformToLoggedUser();
        int count = (int) elasticCaseService.count(requestAsList, loggedUser, Locale.getDefault(), false);
        if (count > 0) {
            return elasticCaseService.search(requestAsList, loggedUser, PageRequest.ofSize(count), Locale.getDefault(), false).getContent();
        } else {
            return new ArrayList<>();
        }
    }

    /**
     * Finds plugin case by provided identifier
     *
     * @param identifier identifier of the plugin
     * @return optional case of the plugin
     */
    @Override
    public Optional<Case> findByIdentifier(String identifier) {
        String query = String.format("processIdentifier:%s AND dataSet.%s.textValue:\"%s\"", PLUGIN_PROCESS_IDENTIFIER,
                PLUGIN_IDENTIFIER_FIELD_ID, identifier);
        List<CaseSearchRequest> requestAsList = List.of(CaseSearchRequest.builder().query(query).build());
        LoggedUser loggedUser = userService.getLoggedOrSystem().transformToLoggedUser();
        Page<Case> result = elasticCaseService.search(requestAsList, loggedUser, PageRequest.ofSize(1), Locale.getDefault(), false);

        return result.hasContent() ? Optional.of(result.getContent().getFirst()) : Optional.empty();
    }

    private String activate(Case pluginCase, Plugin plugin) throws TransitionNotExecutableException,
            PluginIsAlreadyActiveException, NoSuchAlgorithmException {
        if (isPluginActive(pluginCase)) {
            throw new PluginIsAlreadyActiveException(String.format("Plugin with identifier [%s] is already active. Plugin must be inactive.",
                    plugin.getIdentifier()));
        }
        pluginInjector.uninject(pluginCase); // remove potentially outdated meta data
        pluginCase = createOrUpdatePluginCase(plugin, Optional.of(pluginCase));
        pluginCase = doActivation(pluginCase);

        String responseMsg = String.format("Plugin with identifier \"%s\" was activated.", getPluginIdentifier(pluginCase));
        log.info(responseMsg);
        return responseMsg;
    }

    private Case doActivation(Case pluginCase) throws TransitionNotExecutableException {
        String taskId = findTaskIdInCase(pluginCase, PLUGIN_ACTIVATE_TRANS_ID);
        IUser user = userService.getLoggedOrSystem();
        Task activateTask = utils.safelyAssignTask(taskId).getTask();
        return taskService.finishTask(activateTask, user).getCase();
    }

    private Case createOrUpdatePluginCase(Plugin plugin, Optional<Case> pluginCaseOpt) throws NoSuchAlgorithmException {
        Set<String> createdCaseIds = new HashSet<>();
        LoggedUser loggedUser = userService.getLoggedOrSystem().transformToLoggedUser();

        try {
            Case pluginCase = pluginCaseOpt.orElseGet(() -> {
                Case newPluginCase = workflowService.createCaseByIdentifier(PluginConstants.PLUGIN_PROCESS_IDENTIFIER, plugin.getName(),
                        "", loggedUser).getCase();
                createdCaseIds.add(newPluginCase.getStringId());
                return newPluginCase;
            });

            CreateOrUpdateOutcome epOutcome = createOrUpdateEntryPointCases(plugin.getEntryPoints().values(), loggedUser,
                    utils.getPluginEntryPoints(pluginCase));

            createdCaseIds.addAll(epOutcome.getCreatedCaseIds());
            Set<String> epToBeRemovedIds = new HashSet<>(getPluginEntryPointIds(pluginCase));
            epToBeRemovedIds.removeAll(epOutcome.getSubjectCaseIds());

            Map<String, Map<String, Object>> dataToSet = new HashMap<>();
            dataToSet.put(PLUGIN_IDENTIFIER_FIELD_ID, Map.of("value", plugin.getIdentifier(), "type",
                    FieldType.TEXT.getName()));
            dataToSet.put(PLUGIN_NAME_FIELD_ID, Map.of("value", plugin.getName(), "type", FieldType.TEXT.getName()));
            dataToSet.put(PLUGIN_URL_FIELD_ID, Map.of("value", plugin.getUrl(), "type", FieldType.TEXT.getName()));
            dataToSet.put(PLUGIN_PORT_FIELD_ID, Map.of("value", String.valueOf(plugin.getPort()), "type",
                    FieldType.NUMBER.getName()));
            dataToSet.put(PLUGIN_ENTRY_POINTS_FIELD_ID, Map.of("value", epOutcome.getSubjectCaseIds(), "type",
                    FieldType.CASE_REF.getName()));

            String taskId = findTaskIdInCase(pluginCase, PLUGIN_ACTIVATE_TRANS_ID);
            dataService.setData(taskId, ImportHelper.populateDatasetWithObject(dataToSet));

            removeEntryPointCases(epToBeRemovedIds);

            return pluginCase;
        } catch (Exception rethrow) {
            removeCases(createdCaseIds);
            throw rethrow;
        }
    }

    private CreateOrUpdateOutcome createOrUpdateEntryPointCases(Collection<EntryPoint> entryPoints, LoggedUser loggedUser,
                                                                List<Case> existingEpCases) throws NoSuchAlgorithmException {
        CreateOrUpdateOutcome outcome = new CreateOrUpdateOutcome();

        try {
            for (EntryPoint entryPoint : entryPoints) {
                GetOrCreateOutcome epOutcome = getOrCreateEntryPointCase(entryPoint, loggedUser, existingEpCases);
                Case entryPointCase = epOutcome.getSubjectCase();
                if (epOutcome.isNew()) {
                    outcome.addCreatedAndSubjectCaseId(entryPointCase.getStringId());
                } else {
                    outcome.addSubjectCaseId(entryPointCase.getStringId());
                }

                CreateOrUpdateOutcome methodOutcome = createOrUpdateMethodCases(entryPoint, loggedUser,
                        utils.getEntryPointMethods(entryPointCase));
                outcome.addAllCreatedCaseId(methodOutcome.getCreatedCaseIds());
                Set<String> methodToBeRemovedIds = new HashSet<>(getEntryPointMethodIds(entryPointCase));
                methodToBeRemovedIds.removeAll(methodOutcome.getSubjectCaseIds());

                Map<String, Map<String, Object>> dataToSet = new HashMap<>();
                dataToSet.put(ENTRY_POINT_NAME_FIELD_ID, Map.of("value", entryPoint.getName(), "type",
                        FieldType.TEXT.getName()));
                dataToSet.put(ENTRY_POINT_METHODS_FIELD_ID, Map.of("value", methodOutcome.getSubjectCaseIds(),
                        "type", FieldType.CASE_REF.getName()));

                String taskId = findTaskIdInCase(entryPointCase, ENTRY_POINT_DETAIL_TRANS_ID);
                dataService.setData(taskId, ImportHelper.populateDatasetWithObject(dataToSet));

                removeCases(methodToBeRemovedIds);
            }

            return outcome;
        } catch (Exception rethrow) {
            removeCases(outcome.getCreatedCaseIds());
            throw rethrow;
        }
    }

    private GetOrCreateOutcome getOrCreateEntryPointCase(EntryPoint ep, LoggedUser loggedUser, List<Case> existingEpCases) {
        Optional<Case> epCaseOpt = existingEpCases.stream()
                .filter((aCase) -> getEntryPointName(aCase).equals(ep.getName()))
                .findFirst();
        Case epCase = epCaseOpt.orElseGet(() -> workflowService.createCaseByIdentifier(ENTRY_POINT_PROCESS_IDENTIFIER,
                ep.getName(), "", loggedUser).getCase());
        return new GetOrCreateOutcome(epCase, epCaseOpt.isEmpty());
    }

    private CreateOrUpdateOutcome createOrUpdateMethodCases(EntryPoint entryPoint, LoggedUser loggedUser,
                                                            List<Case> existingMethodCases) throws NoSuchAlgorithmException {
        CreateOrUpdateOutcome outcome = new CreateOrUpdateOutcome();

        try {
            for (Method method : entryPoint.getMethods().values()) {
                GetOrCreateOutcome methodOutcome = getOrCreateMethodCase(method, loggedUser, method.getArgTypes(), existingMethodCases);

                Case methodCase = methodOutcome.getSubjectCase();
                if (methodOutcome.isNew()) {
                    outcome.addCreatedAndSubjectCaseId(methodCase.getStringId());
                } else {
                    outcome.addSubjectCaseId(methodCase.getStringId());
                }

                Map<String, Map<String, Object>> dataToSet = new HashMap<>();
                dataToSet.put(METHOD_NAME_FIELD_ID, Map.of("value", method.getName(),
                        "type", FieldType.TEXT.getName()));
                dataToSet.put(METHOD_ARGUMENTS_FIELD_ID, Map.of("value", method.getArgTypes().stream().map(Class::getName).toList(), "type",
                        FieldType.STRING_COLLECTION.getName()));
                dataToSet.put(METHOD_RETURN_TYPE_FIELD_ID, Map.of("value", method.getReturnType().getName(), "type",
                        FieldType.TEXT.getName()));
                dataToSet.put(METHOD_HASHED_SIGNATURE_FIELD_ID, Map.of("value", methodOutcome.getAdditionalData(),
                        "type", FieldType.TEXT.getName()));

                String taskId = findTaskIdInCase(methodCase, METHOD_DETAIL_TRANS_ID);
                dataService.setData(taskId, ImportHelper.populateDatasetWithObject(dataToSet));
            }

            return outcome;
        } catch (Exception rethrow) {
            removeCases(outcome.getCreatedCaseIds());
            throw rethrow;
        }
    }

    private GetOrCreateOutcome getOrCreateMethodCase(Method method, LoggedUser loggedUser, List<Class<?>> argTypes,
                                                     List<Case> existingMethodCases)
            throws NoSuchAlgorithmException {
        String hashedSignature = PluginUtils.hashMethodSignature(method.getName(), argTypes);
        Optional<Case> methodCaseOpt = existingMethodCases.stream()
                .filter((aCase) -> getMethodSignatureHash(aCase).equals(hashedSignature))
                .findFirst();
        Case methodCase = methodCaseOpt.orElseGet(() -> workflowService.createCaseByIdentifier(METHOD_PROCESS_IDENTIFIER,
                method.getName(), "", loggedUser).getCase());
        return new GetOrCreateOutcome(methodCase, methodCaseOpt.isEmpty(), hashedSignature);
    }

    private void removePluginCase(Case pluginCase) {
        removeEntryPointCases(utils.getPluginEntryPoints(pluginCase));
        workflowService.deleteCase(pluginCase);
    }

    private void removeEntryPointCases(Set<String> entryPointCaseIds) {
        removeEntryPointCases(entryPointCaseIds.stream().map(workflowService::findOne).collect(Collectors.toList()));
    }

    private void removeEntryPointCases(List<Case> entryPointCases) {
        for (Case entryPointCase : entryPointCases) {
            for (Case methodCase : utils.getEntryPointMethods(entryPointCase)) {
                workflowService.deleteCase(methodCase);
            }
            workflowService.deleteCase(entryPointCase);
        }
    }

    private void removeCases(Set<String> caseIds) {
        for (String caseId : caseIds) {
            Case caseToRemove = workflowService.findOne(caseId);
            if (caseToRemove != null) {
                workflowService.deleteCase(caseId);
            }
        }
    }
}
