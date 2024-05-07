package com.netgrif.application.engine.integrations.plugins.service;

import com.netgrif.application.engine.integration.plugins.domain.Plugin;
import com.netgrif.application.engine.integration.plugins.repository.PluginRepository;
import com.netgrif.application.engine.integrations.plugins.mock.MockPlugin;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles({"test"})
public class PluginRegistrationServiceTest {

    @Autowired
    private PluginRepository repository;

    @BeforeEach
    public void before() {
        repository.deleteAll();
    }

    @Test
    public void testRegistrationDeactivationAndActivation() {
        MockPlugin.registerOrActivatePlugin();
        Plugin foundPlugin = repository.findByIdentifier(MockPlugin.mockIdentifier);
        assert foundPlugin != null;
        assert foundPlugin.isActive();

        MockPlugin.deactivatePlugin();
        foundPlugin = repository.findByIdentifier(MockPlugin.mockIdentifier);
        assert foundPlugin != null;
        assert !foundPlugin.isActive();

        MockPlugin.registerOrActivatePlugin();
        foundPlugin = repository.findByIdentifier(MockPlugin.mockIdentifier);
        assert foundPlugin != null;
        assert foundPlugin.isActive();
    }

    @Test
    public void testUnregister() {
        MockPlugin.registerOrActivatePlugin();
        Plugin foundPlugin = repository.findByIdentifier(MockPlugin.mockIdentifier);
        assert foundPlugin != null;

        MockPlugin.unregisterPlugin();
        foundPlugin = repository.findByIdentifier(MockPlugin.mockIdentifier);
        assert foundPlugin == null;
    }

}
