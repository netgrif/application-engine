package com.netgrif.application.engine.startup

import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.auth.service.interfaces.IUserService
import com.netgrif.application.engine.orgstructure.groups.config.GroupConfigurationProperties
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.PetriNet
import com.netgrif.application.engine.petrinet.domain.VersionType
import com.netgrif.application.engine.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty

@ConditionalOnProperty(value = "nae.group.default.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Component
@Slf4j
public class GroupRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ImportHelper helper

    @Autowired
    private INextGroupService nextGroupService

    @Autowired
    private IUserService userService

    @Autowired
    private SystemUserRunner systemCreator

    @Autowired
    private IPetriNetService petriNetService

    @Autowired
    private GroupConfigurationProperties groupProperties

    private static final String GROUP_FILE_NAME = "engine-processes/org_group.xml";
    private static final String GROUP_PETRINET_IDENTIFIER = "org_group"
    public static final String DEFAULT_GROUP_TITLE = "Default system group"

    @Override
    void run(String... args) throws Exception {
        createDefaultGroup()
    }

    Optional<PetriNet> createDefaultGroup() {
        PetriNet group
        if ((group = petriNetService.getNewestVersionByIdentifier(GROUP_PETRINET_IDENTIFIER)) != null) {
            log.info("Petri net for groups has already been imported.")
            return Optional.of(group)
        }

        Optional<PetriNet> groupNet =  helper.createNet(GROUP_FILE_NAME, VersionType.MAJOR, systemCreator.loggedSystem)

        if (!groupNet.present) {
            log.error("Import of petri net for groups failed!")
            return groupNet
        }
        if (groupProperties.isSystemEnabled())
            nextGroupService.createDefaultSystemGroup(userService.getLoggedOrSystem())
        return groupNet;
    }
}
