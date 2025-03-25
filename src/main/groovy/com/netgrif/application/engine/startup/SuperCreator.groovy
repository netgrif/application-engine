package com.netgrif.application.engine.startup

import com.netgrif.application.engine.authentication.domain.*
import com.netgrif.application.engine.authentication.domain.params.IdentityParams
import com.netgrif.application.engine.authentication.service.interfaces.IAuthorityService
import com.netgrif.application.engine.authentication.service.interfaces.IIdentityService
import com.netgrif.application.engine.authorization.service.interfaces.IRoleService
import com.netgrif.application.engine.configuration.properties.SuperAdminConfiguration
import com.netgrif.application.engine.orgstructure.groups.interfaces.INextGroupService
import com.netgrif.application.engine.petrinet.domain.dataset.TextField
import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

import java.util.stream.Collectors

@Slf4j
@Component
@CompileStatic
@ConditionalOnProperty(value = "admin.create-super", matchIfMissing = true)
class SuperCreator extends AbstractOrderedCommandLineRunner {

    @Autowired
    private IAuthorityService authorityService

    @Autowired
    private IIdentityService identityService

    @Autowired
    private INextGroupService groupService

    @Autowired
    private IRoleService roleService

    @Autowired
    private SuperAdminConfiguration configuration

    private Identity superIdentity

    @Override
    void run(String... strings) {
        log.info("Creating Super identity")
        createSuperUser()
    }

    private void createSuperUser() {
        Authority adminAuthority = authorityService.getOrCreate(Authority.admin)
        Authority systemAuthority = authorityService.getOrCreate(Authority.systemAdmin)

        Optional<Identity> superIdentityOpt = identityService.findByUsername(configuration.email)
        if (superIdentityOpt.isPresent()) {
            log.info("Super identity detected")
            this.superIdentity = superIdentityOpt.get()
            return
        }
        this.superIdentity = identityService.createWithDefaultActor(IdentityParams.with()
                .username(new TextField(configuration.email))
                .firstname(new TextField(configuration.name))
                .lastname(new TextField(configuration.surname))
                .password(new TextField(configuration.password))
                .build())

        Set<String> allRoleIds = roleService.findAll().stream().map { it.stringId }.collect(Collectors.toSet())
        roleService.assignRolesToActor(this.superIdentity.stringId, allRoleIds)
        // todo 2058 app role (authorities)

        log.info("Super identity created with actor")
    }

    void setAllToSuperUser() {
        setAllGroups()
        setAllRoles()
        setAllAuthorities()
        log.info("Super identity updated")
    }

    void setAllGroups() {
        // todo 2058 groups
//        groupService.findAllGroups().each {
//            groupService.addUser(superIdentity, it)
//        }
    }

    void setAllRoles() {
        Set<String> allRoleIds = roleService.findAll().stream().map { it.stringId }.collect(Collectors.toSet())
        roleService.assignRolesToActor(this.superIdentity.stringId, allRoleIds)
    }

    void setAllAuthorities() {
        // todo 2058 app role authorities
//        superIdentity.setAuthorities(authorityService.findAll() as Set<Authority>)
//        superIdentity = userService.save(superIdentity) as IUser
    }

    Identity getSuperIdentity() {
        return this.superIdentity
    }

    LoggedIdentity getLoggedSuper() {
        return superIdentity.toSession()
    }
}
