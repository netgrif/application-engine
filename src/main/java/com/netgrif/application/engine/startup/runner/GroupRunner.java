package com.netgrif.application.engine.startup.runner;

import com.netgrif.auth.config.GroupConfigurationProperties;
import com.netgrif.auth.service.UserService;
import com.netgrif.auth.service.GroupService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(110)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "nae.group.default.enabled", havingValue = "true", matchIfMissing = true)
public class GroupRunner implements ApplicationEngineStartupRunner {

    private final GroupService groupService;
    private final UserService userService;
    private final GroupConfigurationProperties groupProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createDefaultGroup();
    }

    protected void createDefaultGroup() {
        if (groupProperties.isSystemEnabled())
            groupService.create(userService.getLoggedOrSystem());
    }

}
