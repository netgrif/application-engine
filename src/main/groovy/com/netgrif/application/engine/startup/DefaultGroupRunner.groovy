package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authorization.domain.Group
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants
import com.netgrif.application.engine.authorization.domain.params.GroupParams
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
class DefaultGroupRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IGroupService service

    private Group defaultGroup

    @Override
    void run(String... args) throws Exception {
        Optional<Group> groupOpt = service.findByName(GroupConstants.DEFAULT_GROUP_NAME)
        if (groupOpt.isPresent()) {
            this.defaultGroup = groupOpt.get()
            return
        }
        this.defaultGroup = createDefaultGroup()
    }

    Group getDefaultGroup() {
        return this.defaultGroup
    }

    private Group createDefaultGroup() {
        Group defaultGroup = service.create(GroupParams.with()
                .name(new TextField(GroupConstants.DEFAULT_GROUP_NAME))
                .build())

        log.info("Created default group with id [{}]", defaultGroup.stringId)
        return defaultGroup
    }
}
