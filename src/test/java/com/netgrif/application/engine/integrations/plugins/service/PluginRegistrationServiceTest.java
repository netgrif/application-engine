package com.netgrif.application.engine.integrations.plugins.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.integration.plugins.utils.PluginUtils;
import com.netgrif.application.engine.integrations.plugins.mock.MockPlugin;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.domain.QCase;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

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
    private PluginUtils utils;

    private static final int ELASTIC_WAIT_TIME_IN_MS = 2000;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
        helper.createNet("engine-processes/plugin/plugin.xml");
        helper.createNet("engine-processes/plugin/entry_point.xml");
        helper.createNet("engine-processes/plugin/method.xml");
    }

    @Test
    public void testRegistrationDeactivationAndActivation() {
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

        MockPlugin.deactivatePlugin();
        pluginCase = workflowService.findOne(pluginCase.getStringId());
        assert pluginCase != null;
        assert !PluginUtils.isPluginActive(pluginCase);
        assert !pluginCase.getActivePlaces().containsKey("active");
        assert pluginCase.getActivePlaces().get("inactive").equals(1);

        MockPlugin.registerOrActivatePlugin();
        pluginCase = workflowService.findOne(pluginCase.getStringId());
        assert pluginCase != null;
        assert PluginUtils.isPluginActive(pluginCase);
        assert pluginCase.getActivePlaces().get("active").equals(1);
        assert !pluginCase.getActivePlaces().containsKey("inactive");
    }

    @Test
    public void testUnregister() throws InterruptedException {
        MockPlugin.registerOrActivatePlugin();
        Page<Case> pluginCases = workflowService.search(QCase.case$.processIdentifier.eq("plugin"), Pageable.ofSize(2));
        assert pluginCases.getTotalElements() == 1;
        Case pluginCase = pluginCases.getContent().get(0);
        assert pluginCase != null;

        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);

        String pluginCaseId = pluginCase.getStringId();
        String entryPointCaseId = getPluginEntryPointIds(pluginCase).get(0);
        String methodCaseid = getEntryPointMethodIds(workflowService.findOne(entryPointCaseId)).get(0);
        MockPlugin.unregisterPlugin();
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(pluginCaseId));
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(entryPointCaseId));
        assertThrows(IllegalArgumentException.class, () -> workflowService.findOne(methodCaseid));
    }

    // register register
    // register deactivate edit activate
    // register deactivate unregister
    // register unregister
    // unregister missing
    // register with corrupt request -> check rollback
    // activate missing plugin

}
