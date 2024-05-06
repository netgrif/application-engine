package com.netgrif.application.engine.integrations.plugins.service;

import com.netgrif.application.engine.integration.plugins.domain.Plugin;
import com.netgrif.application.engine.integration.plugins.repository.PluginRepository;
import com.netgrif.application.engine.integration.plugins.service.IPluginService;
import com.netgrif.application.engine.integrations.plugins.mock.MockExecutionService;
import com.netgrif.pluginlibrary.core.ExecutionRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles({"test"})
public class PluginServiceTest {

    @Autowired
    private PluginRepository repository;

    @Autowired
    private IPluginService pluginService;

    @Autowired
    private MockExecutionService mockExecutionService;

    private static final String pluginIdentifier = "mock_plugin";

    @BeforeEach
    public void before() {
        repository.deleteAll();
    }

    @Test
    public void testCallRequestAndResponse() {
        Plugin plugin = new Plugin();
        plugin.setIdentifier(pluginIdentifier);
        plugin.setName("mockPlugin");
        plugin.setUrl("localhost");
        plugin.setPort(8090);
        plugin.setActive(true);
        repository.save(plugin);

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
    public void testCallDeactivatedPlugin() {
        Plugin plugin = new Plugin();
        plugin.setIdentifier(pluginIdentifier);
        plugin.setName("mockPlugin");
        plugin.setUrl("localhost");
        plugin.setPort(8090);
        plugin.setActive(false);
        repository.save(plugin);

        assertThrows(IllegalArgumentException.class, () -> pluginService.call(pluginIdentifier, "mockEP", "mockMethod"));
    }
}
