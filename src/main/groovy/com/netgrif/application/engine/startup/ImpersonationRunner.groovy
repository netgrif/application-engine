package com.netgrif.application.engine.startup

import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
class ImpersonationRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    protected ImportHelper helper


    protected static final String IMPERSONATION_CONFIG_FILE_NAME = "engine-processes/impersonation_config.xml"
    public static final String IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER = "impersonation_config"

    protected static final String IMPERSONATION_CONFIG_USER_SELECT_FILE_NAME = "engine-processes/impersonation_users_select.xml"
    public static final String IMPERSONATION_CONFIG_USER_SELECT_PETRI_NET_IDENTIFIER = "impersonation_users_select"

    @Override
    void run(String... args) throws Exception {
        helper.importProcess("Petri net for impersonation config", IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER, IMPERSONATION_CONFIG_FILE_NAME)
        helper.importProcess("Petri net for impersonation user select", IMPERSONATION_CONFIG_USER_SELECT_PETRI_NET_IDENTIFIER, IMPERSONATION_CONFIG_USER_SELECT_FILE_NAME)
    }
}
