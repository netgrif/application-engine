package com.netgrif.application.engine.startup.runner;

import com.netgrif.adapter.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.ApplicationEngineStartupRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RunnerOrder(130)
@RequiredArgsConstructor
public class ImpersonationRunner implements ApplicationEngineStartupRunner {

    public static final String IMPERSONATION_CONFIG_FILE_NAME = "engine-processes/impersonation_config.xml";
    public static final String IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER = "impersonation_config";
    public static final String IMPERSONATION_CONFIG_USER_SELECT_FILE_NAME = "engine-processes/impersonation_users_select.xml";
    public static final String IMPERSONATION_CONFIG_USER_SELECT_PETRI_NET_IDENTIFIER = "impersonation_users_select";

    protected final IPetriNetService petriNetService;
    protected final ImportHelper helper;
    protected final SystemUserRunner systemCreator;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createConfigNets();
    }

    public void createConfigNets() {
        importProcess("Petri net for impersonation config", IMPERSONATION_CONFIG_PETRI_NET_IDENTIFIER, IMPERSONATION_CONFIG_FILE_NAME);
        importProcess("Petri net for impersonation user select", IMPERSONATION_CONFIG_USER_SELECT_PETRI_NET_IDENTIFIER, IMPERSONATION_CONFIG_USER_SELECT_FILE_NAME);
    }

    public Optional<PetriNet> importProcess(final String message, String netIdentifier, String netFileName) {
        PetriNet foundNet = petriNetService.getNewestVersionByIdentifier(netIdentifier);
        if (foundNet != null) {
            log.info("{} has already been imported.", message);
            return Optional.of(foundNet);
        }

        Optional<PetriNet> net = helper.createNet(netFileName, VersionType.MAJOR, systemCreator.getLoggedSystem());
        if (net.isEmpty()) {
            log.error("Import of {} failed!", message);
        }

        return net;
    }

}
