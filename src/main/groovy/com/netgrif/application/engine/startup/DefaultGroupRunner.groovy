package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authorization.domain.Group
import com.netgrif.application.engine.authorization.domain.constants.GroupConstants
import com.netgrif.application.engine.authorization.service.interfaces.IGroupService
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
        service.registerForbiddenKeywords(Set.of(GroupConstants.DEFAULT_GROUP_NAME))
        if (groupOpt.isPresent()) {
            log.info("Found default group with id [{}]", groupOpt.get().stringId)
            this.defaultGroup = groupOpt.get()
            return
        }
        clearCache()
        this.defaultGroup = service.getDefaultGroup()
    }

    Group getDefaultGroup() {
        return this.defaultGroup
    }

    void clearCache() {
        this.defaultGroup = null
    }
}
