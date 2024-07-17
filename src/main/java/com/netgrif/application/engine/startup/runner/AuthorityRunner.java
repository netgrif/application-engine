package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.domain.Authority;
import com.netgrif.application.engine.auth.service.interfaces.IAuthorityService;
import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(6)
@RequiredArgsConstructor
public class AuthorityRunner extends AbstractOrderedApplicationRunner {

    private final IAuthorityService service;

    @Override
    public void apply(ApplicationArguments args) throws Exception {
        service.getOrCreate(Authority.user);
        service.getOrCreate(Authority.admin);
        service.getOrCreate(Authority.systemAdmin);
        service.getOrCreate(Authority.anonymous);
    }

}
