package com.netgrif.application.engine.integration.plugins.service;

import com.google.protobuf.ByteString;
import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.elastic.service.interfaces.IElasticCaseService;
import com.netgrif.application.engine.elastic.web.requestbodies.CaseSearchRequest;
import com.netgrif.application.engine.integration.plugin.injector.PluginInjector;
import com.netgrif.application.engine.integration.plugins.exceptions.PluginIsAlreadyActiveException;
import com.netgrif.application.engine.integration.plugins.outcomes.CreateOrUpdateOutcome;
import com.netgrif.application.engine.integration.plugins.outcomes.GetOrCreateOutcome;
import com.netgrif.application.engine.integration.plugins.properties.PluginConfigProperties;
import com.netgrif.application.engine.integration.plugins.utils.PluginConstants;
import com.netgrif.application.engine.integration.plugins.utils.PluginUtils;
import com.netgrif.application.engine.petrinet.domain.dataset.FieldType;
import com.netgrif.application.engine.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.IDataService;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.pluginlibrary.core.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.SerializationUtils;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static com.netgrif.application.engine.integration.plugins.utils.PluginConstants.*;
import static com.netgrif.application.engine.integration.plugins.utils.PluginUtils.*;

/**
 * Base service, that manages gRPC server on application startup, registers, activates and deactivates plugins, sends
 * plugin execution requests to desired plugin.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(
        value = "nae.plugin.enabled",
        havingValue = "true",
        matchIfMissing = true
)
public class PluginService implements IPluginService {
    private final PluginConfigProperties properties;
    private final IElasticCaseService elasticCaseService;
    private final IWorkflowService workflowService;
    private final IUserService userService;
    private final IDataService dataService;
    private final ITaskService taskService;
    private final PluginInjector pluginInjector;
    private final PluginUtils utils;
    private Server server;

    private static final String LOG_PREFIX = "[gRPC Server] -";

    @PostConstruct
    public void startServer() throws IOException {
        server = ServerBuilder
                .forPort(properties.getPort())
                .addService(new PluginRegistrationService(this))
                .build();
        server.start();
        log.info("{} Started on port {}", LOG_PREFIX, properties.getPort());
    }

    @PreDestroy
    public void stopServer() {
        server.shutdown();
        log.info("{} Stopped server on port {}", LOG_PREFIX, properties.getPort());
    }

    /**
     * Registers provided plugin into repository. If the plugin already exists, it's activated.
     *
     * @param request - plugin to be registered or if already registered, then activated
     * @return activation or registration string message is returned
     */
    @Override
    public String registerOrActivate(RegistrationRequest request) throws PluginIsAlreadyActiveException {
        Optional<Case> existingPluginOpt = findByIdentifier(request.getIdentifier());
        try {
            if (existingPluginOpt.isPresent()) {
                return activate(existingPluginOpt.get(), request);
            } else {
                return register(request);
            }
        } catch (TransitionNotExecutableException e) {
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
     * @param pluginId   plugin identifier, that contains the method to be executed
     * @param entryPoint name of entry point in plugin, that contains the method to be executed
     * @param method     name of method to be executed
     * @param args       arguments to send to plugin method. All args should be the exact type of method input arguments type (not superclass, or subclass)
     * @return the returned object of the executed plugin method
     */
    @Override
    public Object call(String pluginId, String entryPoint, String method, Serializable... args) throws IllegalArgumentException {
        Optional<Case> pluginCaseOpt = findByIdentifier(pluginId);
        if (pluginCaseOpt.isEmpty()) {
            throw new IllegalArgumentException("Plugin with identifier \"" + pluginId + "\" cannot be found");
        }
        Case pluginCase = pluginCaseOpt.get();
        if (!isPluginActive(pluginCase)) {
            throw new IllegalArgumentException("Plugin with name \"" + getPluginName(pluginCase) + "\" is deactivated");
        }
        ManagedChannel channel = ManagedChannelBuilder.forAddress(getPluginUrl(pluginCase), getPluginPort(pluginCase))
                .usePlaintext()
                .build();
        List<ByteString> argBytes = Arrays.stream(args).map(arg -> ByteString.copyFrom(
                Objects.requireNonNull(SerializationUtils.serialize(arg)))).collect(Collectors.toList());
        ExecutionServiceGrpc.ExecutionServiceBlockingStub stub = ExecutionServiceGrpc.newBlockingStub(channel);
        ExecutionResponse responseMessage = stub.execute(ExecutionRequest.newBuilder()
                .setEntryPoint(entryPoint)
                .setMethod(method)
                .addAllArgs(argBytes)
                .build());
        channel.shutdownNow();
        return SerializationUtils.deserialize(responseMessage.getResponse().toByteArray());
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

        return result.hasContent() ? Optional.of(result.getContent().get(0)) : Optional.empty();
    }

    private String register(RegistrationRequest request) throws TransitionNotExecutableException {
        Case pluginCase = createOrUpdatePluginCase(request, Optional.empty());
        pluginCase = doActivation(pluginCase);
        return inject(pluginCase, "registered");
    }

    private String activate(Case pluginCase, RegistrationRequest request) throws TransitionNotExecutableException,
            PluginIsAlreadyActiveException {
        if (isPluginActive(pluginCase)) {
            throw new PluginIsAlreadyActiveException(String.format("Plugin with identifier [%s] is already active. Plugin must be inactive.",
                    request.getIdentifier()));
        }
        pluginCase = createOrUpdatePluginCase(request, Optional.of(pluginCase));
        pluginCase = doActivation(pluginCase);
        return inject(pluginCase, "activated"); // we must also re-inject the plugin in case of there is a change of entry points
    }

    private Case doActivation(Case pluginCase) throws TransitionNotExecutableException {
        String taskId = findTaskIdInCase(pluginCase, PLUGIN_ACTIVATE_TRANS_ID);
        IUser user = userService.getLoggedOrSystem();
        Task activateTask = utils.safelyAssignTask(taskId).getTask();
        return taskService.finishTask(activateTask, user).getCase();
    }

    private String inject(Case plugin, String state) {
        pluginInjector.inject(plugin);

        String responseMsg = String.format("Plugin with identifier \"%s\" was %s.", getPluginIdentifier(plugin), state);
        log.info(responseMsg);
        return responseMsg;
    }

    private Case createOrUpdatePluginCase(RegistrationRequest request, Optional<Case> pluginCaseOpt) {
        Set<String> createdCaseIds = new HashSet<>();
        LoggedUser loggedUser = userService.getLoggedOrSystem().transformToLoggedUser();

        try {
            Case pluginCase = pluginCaseOpt.orElseGet(() -> {
                Case newPluginCase = workflowService.createCaseByIdentifier(PluginConstants.PLUGIN_PROCESS_IDENTIFIER, request.getName(),
                        "", loggedUser).getCase();
                createdCaseIds.add(newPluginCase.getStringId());
                return newPluginCase;
            });

            CreateOrUpdateOutcome epOutcome = createOrUpdateEntryPointCases(request.getEntryPointsList(), loggedUser,
                    utils.getPluginEntryPoints(pluginCase));

            createdCaseIds.addAll(epOutcome.getCreatedCaseIds());
            Set<String> epToBeRemovedIds = new HashSet<>(getPluginEntryPointIds(pluginCase));
            epToBeRemovedIds.removeAll(epOutcome.getSubjectCaseIds());

            Map<String, Map<String, Object>> dataToSet = new HashMap<>();
            dataToSet.put(PLUGIN_IDENTIFIER_FIELD_ID, Map.of("value", request.getIdentifier(), "type",
                    FieldType.TEXT.getName()));
            dataToSet.put(PLUGIN_NAME_FIELD_ID, Map.of("value", request.getName(), "type", FieldType.TEXT.getName()));
            dataToSet.put(PLUGIN_URL_FIELD_ID, Map.of("value", request.getUrl(), "type", FieldType.TEXT.getName()));
            dataToSet.put(PLUGIN_PORT_FIELD_ID, Map.of("value", String.valueOf(request.getPort()), "type",
                    FieldType.NUMBER.getName()));
            dataToSet.put(PLUGIN_ENTRY_POINTS_FIELD_ID, Map.of("value", epOutcome.getSubjectCaseIds(), "type",
                    FieldType.CASE_REF.getName()));

            String taskId = findTaskIdInCase(pluginCase, PLUGIN_ACTIVATE_TRANS_ID);
            dataService.setData(taskId, ImportHelper.populateDatasetAsObjects(dataToSet));

            removeEntryPointCases(epToBeRemovedIds);

            return pluginCase;
        } catch (Exception rethrow) {
            removeCases(createdCaseIds);
            throw rethrow;
        }
    }

    private CreateOrUpdateOutcome createOrUpdateEntryPointCases(List<EntryPoint> entryPoints, LoggedUser loggedUser, List<Case> existingEpCases) {
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
                dataService.setData(taskId, ImportHelper.populateDatasetAsObjects(dataToSet));

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
                                                            List<Case> existingMethodCases) {
        CreateOrUpdateOutcome outcome = new CreateOrUpdateOutcome();

        try {
            for (com.netgrif.pluginlibrary.core.Method method : entryPoint.getMethodsList()) {
                GetOrCreateOutcome methodOutcome = getOrCreateMethodCase(method, loggedUser, existingMethodCases);

                Case methodCase = methodOutcome.getSubjectCase();
                if (methodOutcome.isNew()) {
                    outcome.addCreatedAndSubjectCaseId(methodCase.getStringId());
                } else {
                    outcome.addSubjectCaseId(methodCase.getStringId());
                }

                Map<String, Map<String, Object>> dataToSet = new HashMap<>();
                dataToSet.put(METHOD_NAME_FIELD_ID, Map.of("value", method.getName(),
                        "type", FieldType.TEXT.getName()));
                List<String> argTypesAsString = method.getArgsList().stream()
                        .map(arg -> {
                            Class<?> clazz = (Class<?>) SerializationUtils.deserialize(arg.toByteArray());
                            assert clazz != null;
                            return clazz.getName();
                        })
                        .collect(Collectors.toList());
                dataToSet.put(METHOD_ARGUMENTS_FIELD_ID, Map.of("value", argTypesAsString, "type",
                        FieldType.STRING_COLLECTION.getName()));

                String taskId = findTaskIdInCase(methodCase, METHOD_DETAIL_TRANS_ID);
                dataService.setData(taskId, ImportHelper.populateDatasetAsObjects(dataToSet));
            }

            return outcome;
        } catch (Exception rethrow) {
            removeCases(outcome.getCreatedCaseIds());
            throw rethrow;
        }
    }

    private GetOrCreateOutcome getOrCreateMethodCase(Method method, LoggedUser loggedUser, List<Case> existingMethodCases) {
        Optional<Case> methodCaseOpt = existingMethodCases.stream()
                .filter((aCase) -> getMethodName(aCase).equals(method.getName()))
                .findFirst();
        Case methodCase = methodCaseOpt.orElseGet(() -> workflowService.createCaseByIdentifier(METHOD_PROCESS_IDENTIFIER,
                method.getName(), "", loggedUser).getCase());
        return new GetOrCreateOutcome(methodCase, methodCaseOpt.isEmpty());
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
