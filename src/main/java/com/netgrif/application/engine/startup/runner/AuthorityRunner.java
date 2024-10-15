package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(60)
@RequiredArgsConstructor
public class AuthorityRunner implements ApplicationEngineStartupRunner {

    private final IAuthorityService service;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        service.getOrCreate(Authority.user);
        service.getOrCreate(Authority.admin);
        service.getOrCreate(Authority.systemAdmin);
        service.getOrCreate(Authority.anonymous);
    }

}
