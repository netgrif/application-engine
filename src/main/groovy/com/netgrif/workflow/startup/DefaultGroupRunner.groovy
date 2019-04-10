package com.netgrif.workflow.startup

import com.netgrif.workflow.orgstructure.domain.Group
import com.netgrif.workflow.orgstructure.service.IGroupService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class DefaultGroupRunner extends AbstractOrderedCommandLineRunner {

    public static final String DEFAULT_GROUP_NAME = "Default"

    @Autowired
    private ImportHelper helper

    @Autowired
    private IGroupService groupService

    @Override
    void run(String... strings) throws Exception {
        def group = groupService.findAll().find { it.name == DEFAULT_GROUP_NAME }
        if (group)
            return

        group = new Group(DEFAULT_GROUP_NAME)
        groupService.save(group)
    }
}
