package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.Identity
import com.netgrif.application.engine.authentication.domain.constants.AnonymIdentityConstants
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.authorization.service.interfaces.IUserService
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Slf4j
@Component
@CompileStatic
class AnonymousIdentityRunner extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IUserService userService

    @Autowired
    private IIdentityService identityService

    @Autowired
    private IRoleService roleService

    @Override
    void run(String... args) throws Exception {
        Optional<Identity> anonymOpt = identityService.findByUsername(AnonymIdentityConstants.defaultUsername())
        if (anonymOpt.isPresent()) {
            return
        }
        createAnonymousIdentityWithUser()
    }

    private void createAnonymousIdentityWithUser() {
        Identity anonymIdentity = identityService.createWithDefaultUser(IdentityParams.with()
                .username(new TextField(AnonymIdentityConstants.defaultUsername()))
                .password(new TextField("n/a"))
                .firstname(new TextField(AnonymIdentityConstants.FIRSTNAME))
                .lastname(new TextField(AnonymIdentityConstants.LASTNAME))
                .build())

        Set<String> roleIds = Set.of(roleService.findApplicationRoleByImportId(ApplicationRoleRunner.ANONYMOUS_APP_ROLE).getStringId(),
                roleService.findAnonymousRole().getStringId());
        roleService.assignRolesToActor(anonymIdentity.getMainActorId(), roleIds);

        Set<String> keywords = Set.of(AnonymIdentityConstants.defaultUsername())
        userService.registerForbiddenKeywords(keywords)
        identityService.registerForbiddenKeywords(keywords)

        log.info("Created anonymous identity [{}] with user [{}].", anonymIdentity.stringId, anonymIdentity.mainActorId)
    }
}
