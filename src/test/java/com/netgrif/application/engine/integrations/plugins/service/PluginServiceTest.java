package com.netgrif.application.engine.integrations.plugins.service;

import com.netgrif.application.engine.TestHelper;
import com.netgrif.application.engine.integration.plugins.service.IPluginService;
import com.netgrif.application.engine.integrations.plugins.mock.MockExecutionService;
import com.netgrif.pluginlibrary.service.services.ExecutionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test"})
public class PluginServiceTest {

    @Autowired
    private TestHelper testHelper;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private MockExecutionService mockExecutionService;

    @BeforeEach
    public void before() {
        testHelper.truncateDbs();
    }

    @Test
    public void testCallRequestAndResponse() {
        Object response = pluginService.call("localhost", MockExecutionService.port, "mockEP", "mockMethod", "mockArg1", "mockArg2");

        ExecutionRequest request = mockExecutionService.lastExecutionRequest;

        assert request.getEntryPoint().equals("mockEP");
        assert request.getMethod().equals("mockMethod");
        assert request.getArgsList().size() == 2;

        assert response.equals("mockResponse");
    }
}
