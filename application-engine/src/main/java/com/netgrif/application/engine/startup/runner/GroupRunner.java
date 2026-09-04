package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.config.GroupConfigurationProperties;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.auth.service.GroupService;
import com.netgrif.application.engine.objects.auth.domain.Group;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RunnerOrder(110)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "netgrif.engine.group.default-enabled", havingValue = "true", matchIfMissing = true)
public class GroupRunner implements ApplicationEngineStartupRunner {

    private final GroupService groupService;
    private final UserService userService;
    private final GroupConfigurationProperties groupProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (groupProperties.isSystemEnabled()) {
            createDefaultGroup();
        }
    }

    protected void createDefaultGroup() {
        Optional<Group> systemGroupOpt = groupService.findByIdentifier(userService.getSystem().getUsername());
        if (systemGroupOpt.isEmpty()) {
            groupService.create(userService.getSystem());
            log.info("Default system group created.");
        }
    }

}
