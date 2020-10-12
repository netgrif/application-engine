package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.workflow.petrinet.domain.PetriNet
import com.netgrif.workflow.petrinet.service.interfaces.IPetriNetService
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

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

    private static final String GROUP_FILE_NAME = "engine-processes/org_group.xml";
    private static final String GROUP_PETRINET_IDENTIFIER = "org_group"

    @Override
    void run(String... args) throws Exception {
        if(petriNetService.getNewestVersionByIdentifier(GROUP_PETRINET_IDENTIFIER) != null){
            log.info("Petri net for groups has already been imported.")
            return
        }

        Optional<PetriNet> groupNet =  helper.createNet(GROUP_FILE_NAME, "major", systemCreator.loggedSystem)

        if(!groupNet.present){
            log.error("Import of petri net for groups failed!")
            return
        }
        nextGroupService.createDefaultSystemGroup(userService.getLoggedOrSystem())
    }
}
