package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.config.AuthorityConfigurationProperties;
import com.netgrif.application.engine.objects.auth.constants.AuthorizingObject;
import com.netgrif.application.engine.auth.service.AuthorityService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RunnerOrder(60)
@RequiredArgsConstructor
public class AuthorityRunner implements ApplicationEngineStartupRunner {

    private final AuthorityService service;

    private final AuthorityConfigurationProperties authorityConfigurationProperties;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createAll();
    }

    void createAll() {
        List.of(AuthorizingObject.values()).forEach(authority -> service.getOrCreate(authority.name()));
        authorityConfigurationProperties.getAdditionalAuthorizingObjects().forEach(service::getOrCreate);
        log.info("Authorities created.");
    }
}
