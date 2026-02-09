package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.objects.auth.domain.AbstractUser;
import com.netgrif.application.engine.objects.auth.domain.ActorTransformer;
import com.netgrif.application.engine.objects.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.UserService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(50)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "admin.create-system-user", matchIfMissing = true)
public class SystemUserRunner implements ApplicationEngineStartupRunner {

    public static final String SYSTEM_USER_EMAIL = "engine@netgrif.com";
    public static final String SYSTEM_USER_NAME = "application";
    public static final String SYSTEM_USER_SURNAME = "engine";

    private final UserService userService;

    private AbstractUser systemUser;

    public AbstractUser createSystemUser() {
        return userService.createSystemUser();
    }

    public LoggedUser getLoggedSystem() {
        if (systemUser == null) {
            log.warn("System user is null");
            return null;
        }
        return ActorTransformer.toLoggedUser(systemUser);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.systemUser = createSystemUser();
    }

}
