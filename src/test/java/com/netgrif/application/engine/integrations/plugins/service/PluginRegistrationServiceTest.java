package com.netgrif.application.engine.integrations.plugins.service;

import com.google.protobuf.ByteString;
import com.netgrif.application.engine.TestHelper;
import com.netgrif.core.auth.domain.LoggedUser;
import com.netgrif.auth.service.UserService;
import com.netgrif.application.engine.integration.plugins.utils.PluginUtils;
import com.netgrif.application.engine.integrations.plugins.mock.MockPlugin;
import com.netgrif.core.petrinet.domain.throwable.TransitionNotExecutableException;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.core.workflow.domain.Case;
import com.netgrif.core.workflow.domain.QCase;
import com.netgrif.core.workflow.domain.Task;
import com.netgrif.application.engine.workflow.service.interfaces.ITaskService;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.pluginlibrary.core.domain.EntryPoint;
import com.netgrif.pluginlibrary.core.domain.Method;
import com.netgrif.pluginlibrary.core.domain.Plugin;
import com.netgrif.pluginlibrary.core.utils.AbstractObjectParser;
import com.netgrif.pluginlibrary.services.service.*;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.netgrif.application.engine.integration.plugins.utils.PluginUtils.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
public class PluginRegistrationServiceTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper helper;

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IPetriNetService petriNetService;

    @Autowired
    private UserService userService;

    @Autowired
    private ITaskService taskService;

    @Autowired
    private PluginUtils utils;

    private static final int ELASTIC_WAIT_TIME_IN_MS = 2000;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
        helper.createNet("engine-processes/plugin/plugin.xml");
        helper.createNet("engine-processes/plugin/entry_point.xml");
        helper.createNet("engine-processes/plugin/method.xml");
        helper.createNet("plugin/test_plugin_injector.xml");
    }

    @Test
    public void testRegistrationDeactivationAndActivation() throws InterruptedException, TransitionNotExecutableException {
        MockPlugin.registerOrActivatePlugin();

        Page<Case> pluginCases = workflowService.search(QCase.case$.processIdentifier.eq("plugin"), Pageable.ofSize(2));

        assert pluginCases.getTotalElements() == 1;
        Case pluginCase = pluginCases.getContent().get(0);
        assert pluginCase != null;
        assert getPluginName(pluginCase).equals(MockPlugin.mockName);
        assert getPluginIdentifier(pluginCase).equals(MockPlugin.mockIdentifier);
        assert PluginUtils.isPluginActive(pluginCase);
        assert pluginCase.getActivePlaces().get("active").equals(1);
        assert !pluginCase.getActivePlaces().containsKey("inactive");

        List<Case> entryPointCases = utils.getPluginEntryPoints(pluginCase);
        assert entryPointCases.size() == 1;
        Case entryPointCase = entryPointCases.get(0);
        assert getEntryPointName(entryPointCase).equals(MockPlugin.mockEntryPointName);

        List<Case> methodCases = utils.getEntryPointMethods(entryPointCase);
        assert methodCases.size() == 1;
        Case methodCase = methodCases.get(0);
        assert getMethodName(methodCase).equals(MockPlugin.mockMethodName);
        List<String> methodArgs = getMethodArguments(methodCase);
        assert methodArgs.size() == 1;
        assert methodArgs.get(0).equals(MockPlugin.mockArgumentType.getName());
        assert getMethodReturnType(methodCase).equals(MockPlugin.mockOutputType.getName());
        testIsInjected();

        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);
        MockPlugin.deactivatePlugin();
        pluginCase = workflowService.findOne(pluginCase.getStringId());
        assert pluginCase != null;
        assert !PluginUtils.isPluginActive(pluginCase);
        assert !pluginCase.getActivePlaces().containsKey("active");
        assert pluginCase.getActivePlaces().get("inactive").equals(1);
        testIsNotInjected();

        MockPlugin.registerOrActivatePlugin();
        pluginCase = workflowService.findOne(pluginCase.getStringId());
        assert pluginCase != null;
        assert PluginUtils.isPluginActive(pluginCase);
        assert pluginCase.getActivePlaces().get("active").equals(1);
        assert !pluginCase.getActivePlaces().containsKey("inactive");
        testIsInjected();
    }

    @Test
    public void testRegisterAndUnregister() throws InterruptedException, TransitionNotExecutableException {
        MockPlugin.registerOrActivatePlugin();
        Page<Case> pluginCases = workflowService.search(QCase.case$.processIdentifier.eq("plugin"), Pageable.ofSize(2));
        assert pluginCases.getTotalElements() == 1;
        Case pluginCase = pluginCases.getContent().get(0);
        assert pluginCase != null;
        testIsInjected();

        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);

        MockPlugin.unregisterPlugin();
        assertIfAnyCaseExists();
        testIsNotInjected();
    }

    @Test
    public void testRegisterAndRegister() throws InterruptedException {
        MockPlugin.registerOrActivatePlugin();

        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);
        assertThrows(StatusRuntimeException.class, MockPlugin::registerOrActivatePlugin);
    }

    @Test
    public void testRegisterDeactivateEditAndActivate() throws InterruptedException, TransitionNotExecutableException {
        MockPlugin.registerOrActivatePlugin();

        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);
        MockPlugin.deactivatePlugin();

        Case pluginCase = workflowService.searchOne(QCase.case$.processIdentifier.eq("plugin"));
        assert getPluginName(pluginCase).equals(MockPlugin.mockName);

        List<Case> epCases = utils.getPluginEntryPoints(pluginCase);
        assert epCases.size() == 1;
        assert getEntryPointName(epCases.get(0)).equals(MockPlugin.mockEntryPointName);

        List<Case> methodCases = utils.getEntryPointMethods(epCases.get(0));
        assert methodCases.size() == 1;
        assert getMethodName(methodCases.get(0)).equals(MockPlugin.mockMethodName);

        String oldEpCaseId = epCases.get(0).getStringId();
        String oldMethodCaseId = methodCases.get(0).getStringId();
        MockPlugin.mockName = "pluginNewName";
        MockPlugin.mockEntryPointName = "entryPointNewName";
        MockPlugin.mockMethodName = "methodNewName";

        MockPlugin.registerOrActivatePlugin();

        testIsNotInjected(); // will not assert if the old metadata are uninjected

        pluginCase = workflowService.findOne(pluginCase.getStringId());
        assert getPluginName(pluginCase).equals("pluginNewName");

        epCases = utils.getPluginEntryPoints(pluginCase);
        assert epCases.size() == 1;
        assert getEntryPointName(epCases.get(0)).equals("entryPointNewName");
        assert !epCases.get(0).getStringId().equals(oldEpCaseId);
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(oldEpCaseId));

        methodCases = utils.getEntryPointMethods(epCases.get(0));
        assert methodCases.size() == 1;
        assert getMethodName(methodCases.get(0)).equals("methodNewName");
        assert !methodCases.get(0).getStringId().equals(oldMethodCaseId);
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(oldMethodCaseId));
    }

    @Test
    public void testRegisterDeactivateAndUnregister() throws InterruptedException {
        MockPlugin.registerOrActivatePlugin();
        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);
        MockPlugin.deactivatePlugin();

        assertIfAnyCaseNotExists();
        MockPlugin.unregisterPlugin();
        assertIfAnyCaseExists();
    }

    @Test
    public void testUnregisterMissing() {
        assertThrows(StatusRuntimeException.class, MockPlugin::unregisterPlugin);
    }


    @Test
    public void testRegistrationWithOverloadingMethods() throws InterruptedException {
        String pluginIdentifier = "identifier";

        Plugin plugin = Plugin.builder()
                .identifier(pluginIdentifier)
                .name("pluginName")
                .url("url")
                .port(1)
                .entryPoints(Map.of(
                    "epName", EntryPoint.builder()
                        .name("epName")
                        .methods(Map.of("method1", Method.builder()
                                .name("method1")
                                .argTypes(List.of(Integer.class))
                                .build(), "method2", Method.builder()
                                .name("method2")
                                .argTypes(List.of(Double.class))
                                .build()))
                        .build()
                    ))
                .build();

        RegistrationRequest req = RegistrationRequest.newBuilder()
                .setPlugin(ByteString.copyFrom(AbstractObjectParser.serialize(plugin)))
                .build();

        MockPlugin.registerOrActivateWithCustomRequest(req);

        Case entryPointCase = workflowService.searchOne(QCase.case$.processIdentifier.eq("entry_point"));
        List<Case> methodCases = utils.getEntryPointMethods(entryPointCase);
        List<String> methodCaseIds = getEntryPointMethodIds(entryPointCase);
        assert methodCases.size() == 2;
        assert !Objects.equals(getMethodSignatureHash(methodCases.get(0)), getMethodSignatureHash(methodCases.get(1)));

        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);

        MockPlugin.deactivatePluginWithCustomRequest(DeactivationRequest.newBuilder().setIdentifier(pluginIdentifier).build());
        MockPlugin.registerOrActivateWithCustomRequest(req);
        entryPointCase = workflowService.findOne(entryPointCase.getStringId());
        assert methodCaseIds.equals(getEntryPointMethodIds(entryPointCase));
        assert utils.getEntryPointMethods(entryPointCase).size() == 2;
        assert workflowService.searchAll(QCase.case$.processIdentifier.eq("method")).getTotalElements() == 2;
    }

    @Test
    public void testRegistrationWithCorruptIdentifier() {
        Plugin plugin = Plugin.builder()
                .identifier("")
                .name("name")
                .url("url")
                .port(1)
                .build();
        RegistrationRequest corruptReq = RegistrationRequest.newBuilder()
                .setPlugin(ByteString.copyFrom(AbstractObjectParser.serialize(plugin)))
                .build();
        assertCorruptRegistrationRequest(corruptReq);
    }

    @Test
    public void testRegistrationWithCorruptName() {
        Plugin plugin = Plugin.builder()
                .identifier("identifier")
                .name("")
                .url("url")
                .port(1)
                .build();
        RegistrationRequest corruptReq = RegistrationRequest.newBuilder()
                .setPlugin(ByteString.copyFrom(AbstractObjectParser.serialize(plugin)))
                .build();
        assertCorruptRegistrationRequest(corruptReq);
    }

    @Test
    public void testRegistrationWithCorruptUrl() {
        Plugin plugin = Plugin.builder()
                .identifier("identifier")
                .name("name")
                .url("")
                .port(1)
                .build();
        RegistrationRequest corruptReq = RegistrationRequest.newBuilder()
                .setPlugin(ByteString.copyFrom(AbstractObjectParser.serialize(plugin)))
                .build();
        assertCorruptRegistrationRequest(corruptReq);
    }

    @Test
    public void testRegistrationWithCorruptEntryPointName() {
        Plugin plugin = Plugin.builder()
                .identifier("")
                .name("name")
                .url("url")
                .port(1)
                .entryPoints(Map.of("name", EntryPoint.builder()
                        .name("")
                        .build()))
                .build();
        RegistrationRequest corruptReq = RegistrationRequest.newBuilder()
                .setPlugin(ByteString.copyFrom(AbstractObjectParser.serialize(plugin)))
                .build();
        assertCorruptRegistrationRequest(corruptReq);
    }

    @Test
    public void testRegistrationWithCorruptMethodName() {
        Plugin plugin = Plugin.builder()
                .identifier("")
                .name("name")
                .url("url")
                .port(1)
                .entryPoints(Map.of("name", EntryPoint.builder()
                                .name("name")
                                .methods(Map.of("name", Method.builder()
                                                .name("")
                                        .build()))
                        .build()))
                .build();
        RegistrationRequest corruptReq = RegistrationRequest.newBuilder()
                .setPlugin(ByteString.copyFrom(AbstractObjectParser.serialize(plugin)))
                .build();
        assertCorruptRegistrationRequest(corruptReq);
    }

    private void assertCorruptRegistrationRequest(RegistrationRequest corruptReq) {
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> MockPlugin.registerOrActivateWithCustomRequest(corruptReq));
        assert e.getStatus().getCode().value() == Status.INVALID_ARGUMENT.getCode().value();
        assertIfAnyCaseExists();
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public void testRegistrationSystemFailure() {
        petriNetService.deletePetriNet(petriNetService.findByImportId("method").get().getStringId(),
                userService.getSystem().transformToLoggedUser()); // create unexpected situation

        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, MockPlugin::registerOrActivatePlugin);
        assert e.getStatus().getCode().value() == Status.INTERNAL.getCode().value();
        assertIfAnyCaseExists();
    }

    @Test
    public void testDeactivationWithCorruptRequest() throws InterruptedException {
        MockPlugin.registerOrActivatePlugin();
        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);

        DeactivationRequest corruptReq = DeactivationRequest.newBuilder()
                .setIdentifier("")
                .build();
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> MockPlugin.deactivatePluginWithCustomRequest(corruptReq));
        assert e.getStatus().getCode().value() == Status.INVALID_ARGUMENT.getCode().value();

        Case pluginCase = workflowService.searchOne(QCase.case$.processIdentifier.eq("plugin"));
        assert isPluginActive(pluginCase);
        assert pluginCase.getActivePlaces().get("active") == 1;
        assert !pluginCase.getActivePlaces().containsKey("inactive");
    }

    @Test
    public void testUnregistrationWithCorruptRequest() throws InterruptedException {
        MockPlugin.registerOrActivatePlugin();
        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);

        UnregistrationRequest corruptReq = UnregistrationRequest.newBuilder()
                .setIdentifier("")
                .build();
        StatusRuntimeException e = assertThrows(StatusRuntimeException.class, () -> MockPlugin.unregisterPluginWithCustomRequest(corruptReq));
        assert e.getStatus().getCode().value() == Status.INVALID_ARGUMENT.getCode().value();
        assertIfAnyCaseNotExists();
    }

    private void testIsInjected() throws TransitionNotExecutableException {
        Case testCase = doTestInjection("injection");
        assert (Boolean) testCase.getFieldValue("is_injected"): "Plugin is not injected";
    }

    private void testIsNotInjected() throws TransitionNotExecutableException {
        Case testCase = doTestInjection("uninjection");
        assert !((Boolean) testCase.getFieldValue("is_injected")): "Plugin is still injected";
    }

    private Case doTestInjection(String transitionId) throws TransitionNotExecutableException {
        LoggedUser loggedUser = userService.getSystem().transformToLoggedUser();
        Case testCase = workflowService.createCaseByIdentifier("test_plugin_injector", "", "", loggedUser).getCase();
        String taskId = PluginUtils.findTaskIdInCase(testCase, transitionId);
        Task injectionTask = taskService.assignTask(taskId).getTask();
        return taskService.finishTask(injectionTask, loggedUser.transformToUser()).getCase();
    }

    private void assertIfAnyCaseExists() {
        assert workflowService.searchOne(QCase.case$.processIdentifier.eq("plugin")) == null;
        assert workflowService.searchOne(QCase.case$.processIdentifier.eq("entry_point")) == null;
        assert workflowService.searchOne(QCase.case$.processIdentifier.eq("method")) == null;
    }

    private void assertIfAnyCaseNotExists() {
        assert workflowService.searchOne(QCase.case$.processIdentifier.eq("plugin")) != null;
        assert workflowService.searchOne(QCase.case$.processIdentifier.eq("entry_point")) != null;
        assert workflowService.searchOne(QCase.case$.processIdentifier.eq("method")) != null;
    }

}
