package com.netgrif.application.engine.startup.runner;

import com.netgrif.auth.service.RealmService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import com.netgrif.core.auth.web.requestbodies.RealmCreateRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RunnerOrder(69)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "realm.create-default", matchIfMissing = true)
public class DefaultRealmRunner implements ApplicationEngineStartupRunner {

    private final RealmService realmService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (realmService.getDefaultRealm().isEmpty()) {
            RealmCreateRequest createRequest = new RealmCreateRequest();
            createRequest.setName("Default");
            createRequest.setDescription("Default realm");
            createRequest.setAdminRealm(true);
            createRequest.setDefaultRealm(true);
            realmService.createRealm(createRequest);
        }
    }
}
