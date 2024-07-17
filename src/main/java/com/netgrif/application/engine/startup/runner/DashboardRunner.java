package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.petrinet.domain.PetriNet;
import com.netgrif.application.engine.petrinet.domain.VersionType;
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService;
import com.netgrif.application.engine.startup.AbstractOrderedApplicationRunner;
import com.netgrif.application.engine.startup.ImportHelper;
import com.netgrif.application.engine.startup.annotation.RunnerOrder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RunnerOrder(14)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "nae.dashboard.enabled", matchIfMissing = false)
public class DashboardRunner extends AbstractOrderedApplicationRunner {

    public static final String DASHBOARD_NET_IDENTIFIER = "dashboard";
    public static final String DASHBOARD_TILE_NET_IDENTIFIER = "dashboard_tile";
    private static final String DASHBOARD_TILE_FILE_NAME = "engine-processes/dashboard_tile.xml";
    private static final String DASHBOARD_FILE_NAME = "engine-processes/dashboard.xml";

    private final IPetriNetService petriNetService;
    private final ImportHelper helper;
    private final SystemUserRunner systemCreator;

    @Override
    public void apply(ApplicationArguments args) throws Exception {
        createDashboardNet();
        createDashboardTileNet();
    }

    public Optional<PetriNet> createDashboardNet() {
        return importProcess("Petri net for filters", DASHBOARD_NET_IDENTIFIER, DASHBOARD_FILE_NAME);
    }

    public Optional<PetriNet> createDashboardTileNet() {
        return importProcess("Petri net for filter preferences", DASHBOARD_TILE_NET_IDENTIFIER, DASHBOARD_TILE_FILE_NAME);
    }

    public Optional<PetriNet> importProcess(final String message, String netIdentifier, String netFileName) {
        PetriNet filter = petriNetService.getNewestVersionByIdentifier(netIdentifier);
        if (filter != null) {
            log.info("{} has already been imported.", message);
            return Optional.of(filter);
        }

        Optional<PetriNet> filterNet = helper.createNet(netFileName, VersionType.MAJOR, systemCreator.getLoggedSystem());

        if (filterNet.isEmpty()) {
            log.error("Import of {} failed!", message);
        }

        return filterNet;
    }


}
