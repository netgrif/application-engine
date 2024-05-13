package com.netgrif.application.engine.integrations.plugins.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.integration.plugins.service.IPluginService;
import com.netgrif.application.engine.integrations.plugins.mock.MockExecutionService;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.workflow.domain.Case;
import com.netgrif.application.engine.workflow.service.interfaces.IWorkflowService;
import com.netgrif.pluginlibrary.core.ExecutionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static com.netgrif.application.engine.integration.plugins.utils.PluginConstants.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
public class PluginServiceTest {

    @Autowired
    private IWorkflowService workflowService;

    @Autowired
    private IUserService userService;

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private ImportHelper helper;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private MockExecutionService mockExecutionService;

    private static final String pluginIdentifier = "mock_plugin";
    private static final int ELASTIC_WAIT_TIME_IN_MS = 2000;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
        helper.createNet("engine-processes/plugin/plugin.xml");
    }

    private void createMockPluginCase(boolean isActive) {
        Case pluginCase = workflowService.createCaseByIdentifier("plugin", "", "",
                userService.getSystem().transformToLoggedUser()).getCase();

        pluginCase.getDataSet().get(PLUGIN_IDENTIFIER_FIELD_ID).setValue(pluginIdentifier);
        pluginCase.getDataSet().get(PLUGIN_NAME_FIELD_ID).setValue("mockPlugin");
        pluginCase.getDataSet().get(PLUGIN_URL_FIELD_ID).setValue("localhost");
        pluginCase.getDataSet().get(PLUGIN_PORT_FIELD_ID).setValue(8090f);
        pluginCase.getDataSet().get(PLUGIN_ACTIVE_FIELD_ID).setValue(isActive);

        workflowService.save(pluginCase);
    }

    @Test
    public void testCallRequestAndResponse() throws InterruptedException {
        createMockPluginCase(true);
        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);

        Object response = pluginService.call(pluginIdentifier, "mockEP", "mockMethod", "mockArg1", "mockArg2");

        ExecutionRequest request = mockExecutionService.lastExecutionRequest;

        assert request.getEntryPoint().equals("mockEP");
        assert request.getMethod().equals("mockMethod");
        assert request.getArgsList().size() == 2;

        assert response.equals("mockResponse");
    }

    @Test
    public void testCallMissingPlugin() {
        assertThrows(IllegalArgumentException.class, () -> pluginService.call("missingIdentifier", "missingEP", "missingMethod"));
    }

    @Test
    public void testCallDeactivatedPlugin() throws InterruptedException {
        createMockPluginCase(false);
        Thread.sleep(ELASTIC_WAIT_TIME_IN_MS);

        assertThrows(IllegalArgumentException.class, () -> pluginService.call(pluginIdentifier, "mockEP", "mockMethod"));
    }
}
