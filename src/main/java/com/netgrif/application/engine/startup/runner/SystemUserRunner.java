package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.domain.IUser;
import com.netgrif.application.engine.auth.domain.LoggedUser;
import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(7)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "admin.create-system-user", matchIfMissing = true)
public class SystemUserRunner extends AbstractOrderedApplicationRunner {

    public static final String SYSTEM_USER_EMAIL = "engine@netgrif.com";
    public static final String SYSTEM_USER_NAME = "application";
    public static final String SYSTEM_USER_SURNAME = "engine";

    private final IUserService service;

    private IUser systemUser;

    public IUser createSystemUser() {
        return service.createSystemUser();
    }

    public LoggedUser getLoggedSystem() {
        if (systemUser == null) {
            log.warn("System user is null");
            return null;
        }
        return this.systemUser.transformToLoggedUser();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        this.systemUser = createSystemUser();
    }

}
