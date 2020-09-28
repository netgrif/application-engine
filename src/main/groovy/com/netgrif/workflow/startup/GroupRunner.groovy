package com.netgrif.workflow.startup

import com.netgrif.workflow.auth.service.interfaces.IUserService;
import com.netgrif.workflow.orgstructure.groups.interfaces.INextGroupService
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@ConditionalOnProperty(value = "nae.group.default.enabled",
        havingValue = "true",
        matchIfMissing = true)
@Component
public class GroupRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private ImportHelper helper

    @Autowired
    private INextGroupService nextGroupService

    @Autowired
    private IUserService userService;

    private static final String GROUP_FILE_NAME = "engine-processes/org_group.xml";

    @Override
    void run(String... args) throws Exception {
        helper.createNet(GROUP_FILE_NAME, "major").get()
        nextGroupService.createGroup("Default system group", userService.getLoggedOrSystem())
    }
}
