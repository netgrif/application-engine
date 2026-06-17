package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.adapter.spring.petrinet.service.ProcessRoleService;
import com.netgrif.application.engine.objects.petrinet.domain.I18nString;
import com.netgrif.application.engine.objects.petrinet.domain.roles.ProcessRole;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Slf4j
@Component
@Profile("!update")
@RunnerOrder(40)
@RequiredArgsConstructor
public class DefaultRoleRunner implements ApplicationEngineStartupRunner {

    private final ProcessRoleService processRoleService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Creating default process role");
        Page<ProcessRole> role = processRoleService.findAllByImportId(ProcessRole.DEFAULT_ROLE, Pageable.ofSize(1));
        if (role != null && !role.isEmpty()) {
            log.info("Default role already exists");
            return;
        }

        ProcessRole defaultRole = new com.netgrif.application.engine.adapter.spring.petrinet.domain.roles.ProcessRole();
        defaultRole.setImportId(ProcessRole.DEFAULT_ROLE);
        defaultRole.setName(new I18nString(ProcessRole.DEFAULT_ROLE));
        defaultRole.setDescription("Default system process role");
        defaultRole.setEvents(new LinkedHashMap<>());
        defaultRole = processRoleService.save(defaultRole);

        if (defaultRole == null) {
            log.error("Error saving default process role");
        }
    }

}
