package com.netgrif.application.engine.startup.runner;

import com.netgrif.application.engine.auth.service.interfaces.IUserService;
import com.netgrif.application.engine.orgstructure.groups.config.GroupConfigurationProperties;
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService;
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
@RunnerOrder(11)
@RequiredArgsConstructor
@ConditionalOnProperty(value = "nae.group.default.enabled", havingValue = "true", matchIfMissing = true)
public class GroupRunner extends AbstractOrderedApplicationRunner {

    public static final String DEFAULT_GROUP_TITLE = "Default system group";

    private static final String GROUP_FILE_NAME = "engine-processes/org_group.xml";
    private static final String GROUP_PETRINET_IDENTIFIER = "org_group";

    private final ImportHelper helper;
    private final INextGroupService nextGroupService;
    private final IUserService userService;
    private final SystemUserRunner systemCreator;
    private final IPetriNetService petriNetService;
    private final GroupConfigurationProperties groupProperties;

    @Override
    public void apply(ApplicationArguments args) throws Exception {
        createDefaultGroup();
    }

    protected Optional<PetriNet> createDefaultGroup() {
        PetriNet group;
        if ((group = petriNetService.getNewestVersionByIdentifier(GROUP_PETRINET_IDENTIFIER)) != null) {
            log.info("Petri net for groups has already been imported.");
            return Optional.of(group);
        }
        Optional<PetriNet> groupNet = helper.createNet(GROUP_FILE_NAME, VersionType.MAJOR, systemCreator.getLoggedSystem());
        if (groupNet.isEmpty()) {
            log.error("Import of petri net for groups failed!");
            return groupNet;
        }
        if (groupProperties.isSystemEnabled())
            nextGroupService.createDefaultSystemGroup(userService.getLoggedOrSystem());
        return groupNet;
    }

}
